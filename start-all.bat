@echo off
setlocal enabledelayedexpansion
color 0A

echo.
echo ============================================================
echo  WhyLock Complete Startup - All Services
echo ============================================================
echo.
echo This script will start all required services:
echo   1. PostgreSQL Database
echo   2. Redis Cache
echo   3. WhyLock Application
echo.

REM Check if running as admin
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo WARNING: This script needs Administrator privileges
    echo to start services properly.
    echo.
    echo Please right-click this file and select:
    echo "Run as Administrator"
    echo.
    pause
    exit /b 1
)

REM Initialize error tracking
set "ERRORS=0"
set "WARNINGS=0"

echo ============================================================
echo [STEP 1] Starting PostgreSQL Database...
echo ============================================================
echo.

setlocal
REM Start PostgreSQL
for /d %%D in ("C:\Program Files\PostgreSQL\*") do (
    set "PG_PATH=%%D"
)

if "%PG_PATH%"=="" (
    echo [ERROR] PostgreSQL not found!
    echo Please install PostgreSQL from: https://www.postgresql.org/download/
    set /a ERRORS+=1
    set PG_SKIP=1
    goto :skip_postgres
)

net start "postgresql-x64-15" 2>nul
if !ERRORLEVEL! EQU 0 goto :postgres_success

net start "postgresql-x64-14" 2>nul
if !ERRORLEVEL! EQU 0 goto :postgres_success

net start "postgresql-x64-13" 2>nul
if !ERRORLEVEL! EQU 0 goto :postgres_success

echo Attempting to start PostgreSQL manually...
start "PostgreSQL" "%PG_PATH%\bin\pg_ctl.exe" -D "%PG_PATH%\data" start 2>nul
if !ERRORLEVEL! NEQ 0 (
    echo [WARNING] Could not start PostgreSQL - attempting to continue
    set /a WARNINGS+=1
    goto :skip_postgres
)

:postgres_success
timeout /t 3 /nobreak
netstat -ano | findstr ":5432" >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo [OK] PostgreSQL is running ✓
) else (
    echo [WARNING] PostgreSQL port not responding - may still be starting...
    set /a WARNINGS+=1
)

:skip_postgres
endlocal & set "PG_PATH=%PG_PATH%"

echo.
echo ============================================================
echo [STEP 2] Starting Redis Cache...
echo ============================================================
echo.

setlocal
if exist "C:\Program Files\Redis\redis-server.exe" (
    set "REDIS_PATH=C:\Program Files\Redis"
) else if exist "C:\redis\redis-server.exe" (
    set "REDIS_PATH=C:\redis"
) else (
    echo [ERROR] Redis not found!
    echo Please install Redis from: https://github.com/microsoftarchive/redis/releases
    set /a ERRORS+=1
    endlocal & goto :skip_redis
)

net start "Redis" 2>nul
if !ERRORLEVEL! EQU 0 goto :redis_success

echo Attempting to start Redis manually...
start "Redis" "%REDIS_PATH%\redis-server.exe" 2>nul
if !ERRORLEVEL! NEQ 0 (
    echo [WARNING] Could not start Redis - attempting to continue
    set /a WARNINGS+=1
    endlocal & goto :skip_redis
)

:redis_success
timeout /t 2 /nobreak
netstat -ano | findstr ":6379" >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo [OK] Redis is running ✓
) else (
    echo [WARNING] Redis port not responding - may still be starting...
    set /a WARNINGS+=1
)

endlocal

:skip_redis

echo.
echo ============================================================
echo [STEP 3] Creating WhyLock Database...
echo ============================================================
echo.

timeout /t 2 /nobreak

if "%PG_SKIP%"=="1" (
    echo [SKIPPED] PostgreSQL not available - skipping database creation
    set /a WARNINGS+=1
    goto :skip_db
)

REM Check if database exists
"%PG_PATH%\bin\psql.exe" -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'whylock'" 2>nul | findstr "1" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Creating database 'whylock'...
    "%PG_PATH%\bin\psql.exe" -U postgres -c "CREATE DATABASE whylock;" 2>nul
    if !ERRORLEVEL! EQU 0 (
        echo [OK] Database created ✓
    ) else (
        echo [WARNING] Could not create database - may already exist
        set /a WARNINGS+=1
    )
) else (
    echo [OK] Database already exists ✓
)

:skip_db

echo.
echo ============================================================
echo [STEP 4] Starting WhyLock Application...
echo ============================================================
echo.

cd /d "C:\Users\shash\OneDrive\Desktop\whylock" || (
    echo [ERROR] Could not change to application directory
    set /a ERRORS+=1
    goto :build_failed
)

echo Building WhyLock application...
echo.

call mvnw.cmd clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed!
    echo Running build again with detailed output for debugging...
    echo.
    call mvnw.cmd clean package -DskipTests
    set /a ERRORS+=1
    goto :build_failed
)

echo.
echo [OK] Build successful ✓
echo.
echo Starting WhyLock application on port 8085...
echo.

java -jar target\whylock-0.0.1-SNAPSHOT.jar
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application failed to start
    set /a ERRORS+=1
)

:build_failed
echo.
echo.
echo ============================================================
echo  Startup Summary
echo ============================================================
echo  Total Errors:   %ERRORS%
echo  Total Warnings: %WARNINGS%
if %ERRORS% GTR 0 (
    echo.
    echo  Status: FAILED (Please fix errors above)
    color 0C
) else if %WARNINGS% GTR 0 (
    echo.
    echo  Status: COMPLETED WITH WARNINGS
    color 0E
) else (
    echo.
    echo  Status: ALL SYSTEMS OPERATIONAL ✓
    color 0A
)
echo ============================================================
echo.

pause
endlocal
exit /b %ERRORS%
