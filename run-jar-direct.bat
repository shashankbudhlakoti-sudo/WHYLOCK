@echo off
echo.
echo ============================================================
echo  WhyLock - Direct JAR Run (Debug Mode)
echo ============================================================
echo.
echo This will run the application directly.
echo If you see errors, note them down.
echo.
echo Verifying services...
echo.

netstat -ano | findstr ":5432" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] PostgreSQL NOT running on port 5432!
    echo Please start: start-postgresql.bat first
    echo.
    pause
    exit /b 1
)
echo [OK] PostgreSQL running ✓

netstat -ano | findstr ":6379" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Redis NOT running on port 6379!
    echo Please start: start-redis.bat first
    echo.
    pause
    exit /b 1
)
echo [OK] Redis running ✓

echo.
echo Starting WhyLock JAR directly...
echo If application starts successfully, you will see:
echo   "Started WhylockApplication in X seconds"
echo.
echo Accessing: http://localhost:8085
echo.
echo Press Ctrl+C to stop the application
echo.
echo ============================================================
echo.

cd /d "C:\Users\shash\OneDrive\Desktop\whylock"
java -jar target\whylock-0.0.1-SNAPSHOT.jar

pause
