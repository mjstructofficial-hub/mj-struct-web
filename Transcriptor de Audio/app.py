"""Servidor local para transcribir videos sin escribir audio/video al disco."""

from __future__ import annotations

import gc
import io
import subprocess
import sys
import threading
from contextlib import suppress
from pathlib import Path
from urllib.parse import urlparse

import imageio_ffmpeg
import numpy as np
from faster_whisper import WhisperModel
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field


APP_DIR = Path(__file__).resolve().parent
ROOT_DIR = APP_DIR.parent
SAMPLE_RATE = 16_000
# Limita el audio decodificado a unos 100 minutos (192 MB PCM) para proteger
# una laptop con 8 GB de RAM. El límite se aplica en memoria, sin archivos temporales.
MAX_AUDIO_BYTES = 192 * 1024 * 1024

# Se carga una sola vez y se usa en CPU con cuantización int8.
MODEL_NAME = "base"
# Para más precisión a costa de más RAM y tiempo, se puede cambiar por "small".
MODEL: WhisperModel | None = None
# RLock permite que la ruta protegida invoque get_model() durante la primera carga.
MODEL_LOCK = threading.RLock()

FFMPEG = imageio_ffmpeg.get_ffmpeg_exe()

ALLOWED_HOSTS = {
    "youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be", "www.youtu.be",
    "tiktok.com", "www.tiktok.com", "vm.tiktok.com", "vt.tiktok.com",
    "instagram.com", "www.instagram.com", "m.instagram.com",
    "facebook.com", "www.facebook.com", "m.facebook.com", "fb.watch",
}

app = FastAPI(title="Transcriptor local", version="1.0.0")


@app.middleware("http")
async def disable_html_cache(request, call_next):
    from urllib.parse import unquote
    decoded_path = unquote(request.url.path).replace("\\", "/")
    lower_path = decoded_path.lower()
    blocked_prefixes = (
        "/simulador complexivo",
        "/mapapro-mj struct/_work",
        "/transcriptor de audio/.venv",
        "/transcriptor de audio/app.py",
        "/cubicar de vigas/app",
        "/cubicar de vigas/gradle",
        "/.git",
    )
    if any(lower_path == p or lower_path.startswith(p + "/") for p in blocked_prefixes) or lower_path.endswith((".bat", ".lnk", ".pyc")):
        from fastapi.responses import PlainTextResponse
        return PlainTextResponse("Not Found", status_code=404)

    response = await call_next(request)
    if lower_path == "/" or lower_path.endswith(".html"):
        response.headers["Cache-Control"] = "no-store, no-cache, must-revalidate, max-age=0"
        response.headers["Pragma"] = "no-cache"
        response.headers["Expires"] = "0"
    return response


class TranscribeRequest(BaseModel):
    url: str = Field(min_length=8, max_length=2048)


def validate_video_url(raw_url: str) -> str:
    """Solo acepta dominios de los servicios solicitados."""
    url = raw_url.strip()
    parsed = urlparse(url)
    host = (parsed.hostname or "").lower().rstrip(".")
    if parsed.scheme not in {"http", "https"} or host not in ALLOWED_HOSTS:
        raise HTTPException(
            status_code=400,
            detail="Usa un enlace público de YouTube, TikTok, Instagram o Facebook.",
        )
    return url


def extract_pcm_in_memory(url: str) -> bytes:
    """Conecta yt-dlp con ffmpeg por tuberías y devuelve PCM almacenado en RAM."""
    ytdlp_command = [
        # Ejecutar yt-dlp como módulo permite que funcione igual dentro del venv.
        sys.executable, "-m", "yt_dlp",
        "--no-playlist", "--no-progress", "--no-warnings", "--quiet",
        "--no-cache-dir", "--format", "bestaudio[abr<=96]/bestaudio", "--output", "-", url,
    ]
    ffmpeg_command = [
        FFMPEG, "-hide_banner", "-loglevel", "error", "-i", "pipe:0",
        "-vn", "-ac", "1", "-ar", str(SAMPLE_RATE), "-f", "s16le", "pipe:1",
    ]

    ytdlp = None
    ffmpeg = None
    try:
        ytdlp = subprocess.Popen(
            ytdlp_command, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
            bufsize=0,
        )
        assert ytdlp.stdout is not None
        ffmpeg = subprocess.Popen(
            ffmpeg_command, stdin=ytdlp.stdout, stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL, bufsize=0,
        )
        # El padre no debe conservar una copia abierta del extremo de la tubería.
        ytdlp.stdout.close()
        assert ffmpeg.stdout is not None

        pcm = bytearray()
        while True:
            chunk = ffmpeg.stdout.read(1024 * 1024)
            if not chunk:
                break
            pcm.extend(chunk)
            if len(pcm) > MAX_AUDIO_BYTES:
                raise HTTPException(
                    status_code=413,
                    detail="El audio supera el límite de 100 minutos para proteger la memoria.",
                )

        ffmpeg.stdout.close()
        ffmpeg_code = ffmpeg.wait()
        ytdlp_code = ytdlp.wait()
        if ffmpeg_code != 0 or ytdlp_code != 0 or not pcm:
            raise HTTPException(
                status_code=422,
                detail=("No se pudo extraer audio. Comprueba que el enlace sea público, "
                        "válido y accesible desde esta computadora."),
            )
        return bytes(pcm)
    except HTTPException:
        raise
    except (OSError, subprocess.SubprocessError) as exc:
        raise HTTPException(
            status_code=500,
            detail="No se pudo iniciar yt-dlp o ffmpeg. Revisa la instalación.",
        ) from exc
    finally:
        for process in (ffmpeg, ytdlp):
            if process is not None and process.poll() is None:
                with suppress(Exception):
                    process.terminate()
                with suppress(Exception):
                    process.wait(timeout=3)
                if process.poll() is None:
                    with suppress(Exception):
                        process.kill()
        if ffmpeg is not None:
            with suppress(Exception):
                if ffmpeg.stdout:
                    ffmpeg.stdout.close()
        if ytdlp is not None:
            with suppress(Exception):
                if ytdlp.stdout:
                    ytdlp.stdout.close()


def get_model() -> WhisperModel:
    global MODEL
    if MODEL is None:
        # Evita que dos primeras solicitudes carguen el modelo a la vez.
        with MODEL_LOCK:
            if MODEL is None:
                MODEL = WhisperModel(MODEL_NAME, device="cpu", compute_type="int8")
    return MODEL


@app.get("/")
def home() -> FileResponse:
    root_index = ROOT_DIR / "index.html"
    if root_index.exists():
        return FileResponse(root_index)
    return FileResponse(APP_DIR / "index.html")


@app.post("/transcribe")
def transcribe(request: TranscribeRequest) -> dict[str, str]:
    url = validate_video_url(request.url)
    pcm_bytes: bytes | None = None
    audio_buffer: io.BytesIO | None = None
    audio: np.ndarray | None = None
    try:
        pcm_bytes = extract_pcm_in_memory(url)
        # Faster Whisper acepta un ndarray float32; no se crea ningún archivo.
        audio_buffer = io.BytesIO(pcm_bytes)
        audio_i16 = np.frombuffer(audio_buffer.getbuffer(), dtype=np.int16)
        audio = audio_i16.astype(np.float32) / 32768.0
        del audio_i16

        # El modelo CPU compartido solo procesa una transcripción a la vez.
        with MODEL_LOCK:
            model = get_model()
            segments, _info = model.transcribe(audio, beam_size=3, vad_filter=True)
            # La iteración es necesaria: faster-whisper produce segmentos bajo demanda.
            transcript = " ".join(segment.text.strip() for segment in segments).strip()

        if not transcript:
            raise HTTPException(status_code=422, detail="No se detectó voz en el audio.")
        return {"transcript": transcript}
    finally:
        # Suelta referencias a todos los buffers PCM en cuanto termina la petición.
        audio = None
        audio_buffer = None
        pcm_bytes = None
        gc.collect()


# Montar el directorio raíz completo para que Portal, MapaPro, Cubicador y Transcriptor
# funcionen bajo el mismo servidor local http://127.0.0.1:8000
app.mount("/", StaticFiles(directory=str(ROOT_DIR), html=True), name="workspace_root")


