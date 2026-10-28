@echo off
echo ================================================================================
echo           CYBERSENTINEL SOC PLATFORM — DESKTOP APPLICATION LAUNCHER
echo ================================================================================
echo.

if not exist bin (
    echo [INFO] Bin folder not found. Compiling first...
    call build.bat
    if %errorlevel% neq 0 exit /b 1
)

echo [LAUNCHING] Starting CyberSentinel SOC Desktop Application...
start javaw -cp "bin;lib\*" com.cybersentinel.Main

echo [READY] Application started.
