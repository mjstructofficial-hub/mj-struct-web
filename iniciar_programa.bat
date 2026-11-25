@echo off
setlocal
title MJ STRUCT - Servidor Local Unificado (Portal + MapaPro + Cubicador + Transcriptor)
cd /d "%~dp0"

if not exist "index.html" (
    echo No se encontro index.html en la carpeta principal.
    pause
    exit /b 1
)

REM 1. Si existe el entorno virtual del Transcriptor (FastAPI + Faster-Whisper),
REM    iniciamos el servidor completo con soporte de transcripcion IA + todo el sitio web.
if exist "Transcriptor de Audio\.venv\Scripts\python.exe" (
    echo =========================================================================
    echo   MJ STRUCT - SERVIDOR UNIFICADO CON SOPORTE FASTAPI + FASTER-WHISPER
    echo =========================================================================
    echo Iniciando servidor en http://127.0.0.1:8000/index.html ...
    echo Mantenga esta ventana abierta mientras utiliza las herramientas.
    start "" "http://127.0.0.1:8000/index.html?v=%RANDOM%%RANDOM%"
    "Transcriptor de Audio\.venv\Scripts\python.exe" -m uvicorn app:app --app-dir "Transcriptor de Audio" --host 127.0.0.1 --port 8000
    goto end
)

REM 2. Si no esta el venv, usamos el servidor HTTP nativo de Python 3 desde la raiz
where python >nul 2>nul
if not errorlevel 1 (
    set "MJ_PYTHON=python"
) else (
    where py >nul 2>nul
    if errorlevel 1 (
        echo Python 3 no esta disponible. Instala Python y habilita Add Python to PATH.
        pause
        exit /b 1
    )
    set "MJ_PYTHON=py -3"
)

echo Iniciando Portal Web MJ STRUCT desde la raiz...
echo Mantenga esta ventana abierta mientras usa las aplicaciones.
%MJ_PYTHON% -c "import http.server, threading, webbrowser, time, urllib.parse; exec('BLOCKED=(\x27/simulador complexivo\x27,\x27/mapapro-mj struct/_work\x27,\x27/transcriptor de audio/.venv\x27,\x27/transcriptor de audio/app.py\x27,\x27/cubicar de vigas/app\x27,\x27/cubicar de vigas/gradle\x27,\x27/.git\x27)\nclass SafeHandler(http.server.SimpleHTTPRequestHandler):\n def send_head(self):\n  p=urllib.parse.unquote(self.path.split(\x27?\x27)[0]).replace(chr(92),\x27/\x27).lower()\n  if any(p==b or p.startswith(b+\x27/\x27) for b in BLOCKED) or p.endswith((\x27.bat\x27,\x27.lnk\x27,\x27.pyc\x27)):\n   self.send_error(404); return None\n  return super().send_head()\ntry:\n server = http.server.ThreadingHTTPServer((\x27127.0.0.1\x27, 8000), SafeHandler)\nexcept OSError:\n server = http.server.ThreadingHTTPServer((\x27127.0.0.1\x27, 0), SafeHandler)'); url='http://127.0.0.1:'+str(server.server_port)+'/index.html?v='+str(time.time_ns()); print('Abriendo '+url, flush=True); threading.Timer(0.5, webbrowser.open, args=(url,)).start(); server.serve_forever()"

:end
echo.
echo El servidor se ha detenido.
pause
