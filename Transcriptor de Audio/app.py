"""Servidor local para transcribir videos sin escribir audio/video al disco."""

from __future__ import annotations

from pathlib import Path
from posixpath import normpath
from urllib.parse import unquote

from fastapi import FastAPI
from fastapi.responses import FileResponse, PlainTextResponse
from fastapi.staticfiles import StaticFiles

from engine import TranscribeRequest, transcribe_video


APP_DIR = Path(__file__).resolve().parent
ROOT_DIR = APP_DIR.parent

BLOCKED_PREFIXES = (
    "/_privado_no_publicar",
    "/simulador complexivo",
    "/mapapro-mj struct/_work",
    "/transcriptor de audio/.venv",
    "/transcriptor de audio/app.py",
    "/transcriptor de audio/engine.py",
    "/cubicar de vigas/app",
    "/cubicar de vigas/gradle",
    "/.git",
)
BLOCKED_SUFFIXES = (".bat", ".lnk", ".pyc")

app = FastAPI(title="Transcriptor local", version="1.0.0")


def is_blocked_path(path: str) -> bool:
    path = "/" + path.lstrip("/").casefold()
    return path.endswith(BLOCKED_SUFFIXES) or any(
        path == prefix or path.startswith(prefix + "/") for prefix in BLOCKED_PREFIXES
    )


class WorkspaceStaticFiles(StaticFiles):
    def lookup_path(self, path: str):
        full_path, stat_result = super().lookup_path(path)
        if stat_result is None:
            return full_path, stat_result
        try:
            relative_path = Path(full_path).resolve().relative_to(ROOT_DIR)
        except (OSError, RuntimeError, ValueError):
            return "", None
        if is_blocked_path(relative_path.as_posix()):
            return "", None
        return full_path, stat_result


@app.middleware("http")
async def disable_html_cache(request, call_next):
    decoded_path = unquote(request.url.path).replace("\\", "/")
    normalized_path = normpath("/" + decoded_path.lstrip("/")).casefold()
    if is_blocked_path(normalized_path):
        return PlainTextResponse("Not Found", status_code=404)

    response = await call_next(request)
    if normalized_path == "/" or normalized_path.endswith(".html"):
        response.headers["Cache-Control"] = "no-store, no-cache, must-revalidate, max-age=0"
        response.headers["Pragma"] = "no-cache"
        response.headers["Expires"] = "0"
    return response


@app.get("/")
def home() -> FileResponse:
    root_index = ROOT_DIR / "index.html"
    if root_index.exists():
        return FileResponse(root_index)
    return FileResponse(APP_DIR / "index.html")


@app.post("/transcribe")
def transcribe(request: TranscribeRequest) -> dict[str, str]:
    return {"transcript": transcribe_video(request.url)}


# Montar el directorio raíz completo para que Portal, MapaPro, Cubicador y Transcriptor
# funcionen bajo el mismo servidor local http://127.0.0.1:8000
app.mount("/", WorkspaceStaticFiles(directory=str(ROOT_DIR), html=True), name="workspace_root")


