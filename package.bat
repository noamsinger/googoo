@echo off
setlocal enabledelayedexpansion

echo =========================================
echo GooGoo Game - Windows Packaging Script
echo =========================================
echo.

rem Add WiX toolset to PATH if it exists in standard location
if exist "C:\Program Files (x86)\WiX Toolset v3.14\bin" (
    set "PATH=%PATH%;C:\Program Files (x86)\WiX Toolset v3.14\bin"
)
if exist "C:\Program Files\WiX Toolset v3.14\bin" (
    set "PATH=%PATH%;C:\Program Files\WiX Toolset v3.14\bin"
)

rem Check for jpackage
where jpackage >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: jpackage is not available. Please use JDK 16+ that includes jpackage.
    exit /b 1
)

rem Check if portable app-image exists, if not build it
if not exist "target\package\output\GooGoo\GooGoo.exe" (
    echo Portable app-image not found. Running build.bat first...
    call build.bat
    if %errorlevel% neq 0 (
        echo ERROR: build.bat failed.
        exit /b 1
    )
)

echo Creating Windows .exe installer...

rem Check if there is an icon file
set "ICON_ARG="
if exist "src\main\resources\images\googoo-game-icon.ico" (
    set "ICON_ARG=--icon src\main\resources\images\googoo-game-icon.ico"
)

call jpackage ^
  --name "GooGoo" ^
  --app-version "2.1.1" ^
  --input target\package-input ^
  --main-jar googoo-game-remake-2.1.1.jar ^
  --main-class com.game.core.Launcher ^
  --dest target\package\output ^
  --vendor "GooGoo Game Team" ^
  --description "GooGoo - A thrilling space adventure game" ^
  --type exe ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut ^
  !ICON_ARG!

if %errorlevel% neq 0 (
    echo ERROR: Packaging failed.
    exit /b 1
)

echo =========================================
echo Packaging Complete!
echo =========================================
echo Windows installer created at target\package\output\GooGoo-2.1.1.exe
echo.
