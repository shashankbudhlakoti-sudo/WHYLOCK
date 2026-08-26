@echo off
echo.
echo ==========================================
echo  Starting Redis Cache Server
echo ==========================================
echo.

REM Find Redis installation
if exist "C:\Program Files\Redis\redis-server.exe" (
    set "REDIS_PATH=C:\Program Files\Redis"
) else if exist "C:\redis\redis-server.exe" (
    set "REDIS_PATH=C:\redis"
) else (
    echo ERROR: Redis installation not found!
    echo.
    echo Please install Redis from: https://github.com/microsoftarchive/redis/releases
    echo Or use Windows Subsystem for Linux (WSL)
    echo.
    pause
    exit /b 1
)

echo Redis Path: %REDIS_PATH%
echo.
echo Starting Redis server...
echo.

REM Try to start the service
net start "Redis" 2>nul || (
    echo Service not found. Starting server manually...
    start "Redis Server" "%REDIS_PATH%\redis-server.exe"
    timeout /t 3
)

echo.
echo Checking if Redis is running on port 6379...
netstat -ano | findstr ":6379" >nul
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Redis is RUNNING on port 6379! ✓
) else (
    echo.
    echo [WARNING] Port 6379 not detected yet. Waiting...
    timeout /t 3
)

echo.
echo Redis should now be running!
echo Keep this window open while using WhyLock.
echo.
pause
