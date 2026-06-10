@echo off
setlocal enabledelayedexpansion

echo =========================================
echo GooGoo Game - Windows Build Script
echo =========================================
echo.

rem Check for local or system Maven
set "MVN_CMD=mvn"
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    if exist ".maven\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MVN_CMD=.maven\apache-maven-3.9.6\bin\mvn.cmd"
    ) else (
        echo ERROR: Maven mvn is not installed or not in PATH, and local Maven was not found.
        exit /b 1
    )
)

echo Cleaning previous builds...
if exist "target\package" rmdir /s /q "target\package"
if exist "target\package-input" rmdir /s /q "target\package-input"
echo.

echo Building with Maven...
call %MVN_CMD% clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed.
    exit /b 1
)
echo.

echo Copying runtime dependencies...
call %MVN_CMD% dependency:copy-dependencies -DoutputDirectory=target -DincludeScope=runtime
if %errorlevel% neq 0 (
    echo ERROR: Copying dependencies failed.
    exit /b 1
)
echo.

echo Staging files...
if not exist "target\package-input" mkdir "target\package-input"
copy /y "target\*.jar" "target\package-input\" >nul
echo.

echo Creating portable app-image...
call jpackage ^
  --name "GooGoo" ^
  --app-version "2.1.1" ^
  --input target\package-input ^
  --main-jar googoo-game-remake-2.1.1.jar ^
  --main-class com.game.core.Launcher ^
  --dest target\package\output ^
  --vendor "GooGoo Game Team" ^
  --description "GooGoo - A thrilling space adventure game" ^
  --type app-image

if %errorlevel% neq 0 (
    echo ERROR: Creating app-image failed.
    exit /b 1
)

echo =========================================
echo Build Complete!
echo =========================================
echo Portable app-image created at target\package\output\GooGoo\
echo To run the game: .\run.bat
echo.
