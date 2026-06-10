@echo off
setlocal enabledelayedexpansion

echo =========================================
echo GooGoo Game - Windows Launcher
echo =========================================
echo.

set "APP_EXE=target\package\output\GooGoo\GooGoo.exe"

if not exist "%APP_EXE%" (
    echo Portable application not found. Building it first...
    call build.bat
    if !errorlevel! neq 0 (
        echo ERROR: Build failed.
        exit /b 1
    )
    echo.
)

echo Launching GooGoo Game...
start "" "%APP_EXE%"
