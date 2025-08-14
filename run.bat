@echo off
REM JSON Socket Connector Launcher Script for Windows

REM Check if Maven is available
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

echo Building JSON Socket Connector...
mvn clean compile -q

if errorlevel 1 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo Starting JSON Socket Connector...
mvn javafx:run

echo Application closed.
pause