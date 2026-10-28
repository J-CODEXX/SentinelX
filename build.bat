@echo off
setlocal enabledelayedexpansion
echo ================================================================================
echo           CYBERSENTINEL SOC PLATFORM — BUILD AND COMPILATION SCRIPT
echo ================================================================================
echo.

if not exist bin mkdir bin

echo [1/2] Discovering Java source files...
powershell -NoProfile -ExecutionPolicy Bypass -File gen_sources.ps1

echo [2/2] Compiling with javac...
javac -encoding UTF-8 -cp "lib\*" -d bin @sources.txt

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    if exist sources.txt del sources.txt
    exit /b 1
)

if exist sources.txt del sources.txt
echo.
echo [SUCCESS] Compilation completed with 0 errors! Classes compiled to bin\
echo.
