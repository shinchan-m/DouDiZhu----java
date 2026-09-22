@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo Java 17 was not found in PATH.
    pause
    exit /b 1
)

call mvnw.cmd -q -DskipTests package
if errorlevel 1 (
    echo.
    echo Build failed.
    pause
    exit /b 1
)

java -jar target\doudizhu.jar
if errorlevel 1 (
    echo The game exited with an error.
    pause
)
