@echo off
echo.
echo ============================================================
echo  WhyLock - Ready to Run
echo ============================================================
echo.
echo Database already exists - Great!
echo.
echo The system is ready. Let me start the application...
echo.

cd /d "C:\Users\shash\OneDrive\Desktop\whylock"

REM Check if services are running
echo Verifying PostgreSQL...
netstat -ano | findstr ":5432" >nul
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: PostgreSQL not detected on port 5432
    echo Make sure to run: start-postgresql.bat first
    echo.
)

echo Verifying Redis...
netstat -ano | findstr ":6379" >nul
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Redis not detected on port 6379
    echo Make sure to run: start-redis.bat first
    echo.
)

echo All checks done. Starting WhyLock application...
echo.
echo Port: 8085
echo URL:  http://localhost:8085
echo.

java -jar target\whylock-0.0.1-SNAPSHOT.jar

pause
