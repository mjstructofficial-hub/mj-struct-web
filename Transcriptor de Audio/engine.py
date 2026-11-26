"""Motor local de audio a texto con Faster-Whisper en CPU."""

from __future__ import annotations

import gc
import shutil
import subprocess
import sys
import threading
from contextlib import suppress
from pathlib import Path
from urllib.parse import urlparse

import imageio_ffmpeg
import numpy as np
from faster_whisper import WhisperModel
from fastapi import HTTPException
from pydantic import BaseModel, Field


SAMPLE_RATE = 16_000
# Unos 100 minutos de PCM mono de 16 bits. El audio se mantiene en memoria.
MAX_AUDIO_BYTES = 192 * 1024 * 1024
MODEL_NAME = "base"
MODEL: WhisperModel | None = None
MODEL_LOCK = threading.RLock()

TIKTOK_HOSTS = {"tiktok.com", "www.tiktok.com", "vm.tiktok.com", "vt.tiktok.com"}
ALLOWED_HOSTS = TIKTOK_HOSTS | {
    "youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be", "www.youtu.be",
    "instagram.com", "www.instagram.com", "m.instagram.com",
    "facebook.com", "www.facebook.com", "m.facebook.com", "fb.watch",
}
ALLOWED_EXTRACTORS = (
    "youtube$,youtube:clip$,youtube:tab$,TikTok$,vm[.]tiktok$,Instagram$,instagram:story$,"
    "InstagramIOS$,facebook$,facebook:reel$,FacebookPluginsVideo$,FacebookRedirectURL$"
)


class TranscribeRequest(BaseModel):
    url: str = Field(min_length=8, max_length=2048)


def validate_video_url(raw_url: str) -> str:
    url = raw_url.strip()
    try:
        parsed = urlparse(url)
        host = (parsed.hostname or "").lower().rstrip(".")
        allowed = (
            parsed.scheme in {"http", "https"}
            and host in ALLOWED_HOSTS
            and parsed.username is None
            and parsed.password is None
            and parsed.port in {None, {"http": 80, "https": 443}[parsed.scheme]}
            and not any(char.isspace() or ord(char) < 32 or char == "\\" for char in url)
        )
    except ValueError:
        allowed = False
    if not allowed:
        raise HTTPException(400, "Usa un enlace público de YouTube, TikTok, Instagram o Facebook.")
    if host in {"tiktok.com", "www.tiktok.com"} and parsed.path.startswith("/@") and "/video/" in parsed.path:
        return f"https://www.tiktok.com{parsed.path}"
    return url


def extract_pcm_in_memory(url: str, *, node_path: Path | None = None) -> bytearray:
    """Conecta yt-dlp con FFmpeg por tuberías y devuelve PCM en memoria."""
    host = (urlparse(url).hostname or "").lower().rstrip(".")
    ytdlp_command = [
        sys.executable, "-m", "yt_dlp",
        "--ignore-config", "--no-plugin-dirs", "--no-playlist", "--playlist-items", "1",
        "--use-extractors", ALLOWED_EXTRACTORS, "--downloader", "native",
        "--socket-timeout", "15", "--retries", "1", "--fragment-retries", "1",
        "--no-progress", "--no-warnings", "--quiet", "--no-cache-dir",
    ]
    node = str(node_path) if node_path and node_path.is_file() else shutil.which("node")
    if node:
        ytdlp_command.extend(["--js-runtimes", f"node:{node}"])
    if host in TIKTOK_HOSTS:
        # Elegir el audio del video. TikTok ofrece aparte la música de fondo.
        ytdlp_command.extend(["--format-sort", "res:720", "--format", "best[acodec!=none]"])
    else:
        ytdlp_command.extend(["--format", "bestaudio[abr<=96]/bestaudio/best"])
    ytdlp_command.extend(["--output", "-", "--", url])

    ffmpeg_command = [
        imageio_ffmpeg.get_ffmpeg_exe(), "-hide_banner", "-loglevel", "error",
        "-protocol_whitelist", "pipe", "-i", "pipe:0", "-vn", "-ac", "1",
        "-ar", str(SAMPLE_RATE), "-f", "s16le", "pipe:1",
    ]

    ytdlp = None
    ffmpeg = None
    try:
        ytdlp = subprocess.Popen(ytdlp_command, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, bufsize=0)
        assert ytdlp.stdout is not None
        ffmpeg = subprocess.Popen(
            ffmpeg_command, stdin=ytdlp.stdout, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, bufsize=0,
        )
        ytdlp.stdout.close()
        assert ffmpeg.stdout is not None

        pcm = bytearray()
        while chunk := ffmpeg.stdout.read(1024 * 1024):
            if len(pcm) + len(chunk) > MAX_AUDIO_BYTES:
                raise HTTPException(413, "El audio supera el límite de unos 100 minutos para proteger la memoria.")
            pcm.extend(chunk)

        ffmpeg.stdout.close()
        if ffmpeg.wait() != 0 or ytdlp.wait() != 0 or not pcm:
            raise HTTPException(
                422, "No se pudo extraer audio. Comprueba que el enlace sea público y abra desde esta computadora.",
            )
        return pcm
    except (OSError, subprocess.SubprocessError) as error:
        raise HTTPException(500, "No se pudo iniciar la extracción de audio. Revisa la instalación.") from error
    finally:
        for process in (ffmpeg, ytdlp):
            if process is None:
                continue
            if process.stdout is not None:
                process.stdout.close()
            if process.poll() is None:
                with suppress(OSError):
                    process.terminate()
                try:
                    process.wait(timeout=3)
                except subprocess.TimeoutExpired:
                    with suppress(OSError):
                        process.kill()
                    with suppress(subprocess.TimeoutExpired):
                        process.wait(timeout=3)


def get_model() -> WhisperModel:
    global MODEL
    if MODEL is None:
        with MODEL_LOCK:
            if MODEL is None:
                MODEL = WhisperModel(MODEL_NAME, device="cpu", compute_type="int8")
    return MODEL


def transcribe_video(raw_url: str, *, node_path: Path | None = None) -> str:
    url = validate_video_url(raw_url)
    audio: np.ndarray | None = None
    try:
        pcm = extract_pcm_in_memory(url, node_path=node_path)
        audio = np.frombuffer(pcm, dtype="<i2").astype(np.float32)
        del pcm
        audio *= 1.0 / 32768.0

        with MODEL_LOCK:
            model = get_model()
            segments, _info = model.transcribe(audio, beam_size=3, vad_filter=True)
            transcript = " ".join(segment.text.strip() for segment in segments).strip()

        if not transcript:
            raise HTTPException(422, "No se detectó voz en el audio.")
        return transcript
    finally:
        audio = None
        gc.collect()
