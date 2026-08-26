@echo off
echo.
echo ==========================================
echo  Starting PostgreSQL Database Server
echo ==========================================
echo.

REM Find PostgreSQL installation
for /d %%D in ("C:\Program Files\PostgreSQL\*") do (
    set "PG_PATH=%%D"
)

if "%PG_PATH%"=="" (
    echo ERROR: PostgreSQL installation not found!
    echo.
    echo Please install PostgreSQL from: https://www.postgresql.org/download/
    echo.
    pause
    exit /b 1
)

echo PostgreSQL Path: %PG_PATH%
echo.
echo Starting PostgreSQL server...
echo.

REM Try to start the service
net start "postgresql-x64-15" 2>nul || net start "postgresql-x64-14" 2>nul || net start "postgresql-x64-13" 2>nul || (
    echo Service not found. Starting server manually...
    "%PG_PATH%\bin\pg_ctl.exe" -D "%PG_PATH%\data" start
)

echo.
echo PostgreSQL is starting...
echo Waiting 5 seconds...
timeout /t 5 /nobreak

echo.
echo Checking if PostgreSQL is running on port 5432...
netstat -ano | findstr ":5432" >nul
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] PostgreSQL is RUNNING on port 5432! ✓
) else (
    echo.
    echo [WARNING] Port 5432 not detected yet. Waiting...
    timeout /t 3
)

echo.
echo PostgreSQL should now be running!
echo.
pause
