@echo off
setlocal
cd /d "%~dp0"

set "PYTHON=python"
where py >nul 2>nul
if %errorlevel%==0 (
    py -3.11 --version >nul 2>nul
    if not errorlevel 1 set "PYTHON=py -3.11"
)

%PYTHON% --version >nul 2>nul
if errorlevel 1 (
    echo No se encontro Python. Instala Python 3.11 o superior de 64 bits y vuelve a ejecutar este archivo.
    echo Durante la instalacion de Python, activa la opcion "Add Python to PATH".
    pause
    exit /b 1
)

%PYTHON% -c "import sys; raise SystemExit(sys.version_info < (3, 11))"
if errorlevel 1 (
    echo Se requiere Python 3.11 o superior. La version actual es incompatible.
    pause
    exit /b 1
)

if not exist ".venv\Scripts\python.exe" (
    echo Creando entorno virtual...
    %PYTHON% -m venv .venv
    if errorlevel 1 goto error
)

echo Instalando o verificando dependencias...
".venv\Scripts\python.exe" -m pip install --upgrade pip
if errorlevel 1 goto error
".venv\Scripts\python.exe" -m pip install --only-binary=numpy -r requirements.txt
if errorlevel 1 goto error

echo.
echo Abre http://127.0.0.1:8000 en el navegador. Para detener el servidor pulsa Ctrl+C.
start "Transcriptor local" http://127.0.0.1:8000
".venv\Scripts\python.exe" -m uvicorn app:app --host 127.0.0.1 --port 8000
goto end

:error
echo.
echo Hubo un problema al instalar las dependencias. Revisa el mensaje anterior.
pause

:end
endlocal
