@echo off
echo ========================================
echo  WhyLock - Compile Only
echo ========================================
echo.
echo Compiling project...
call mvnw.cmd clean compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo COMPILATION FAILED! Please check the error messages above.
    pause
    exit /b 1
)
echo.
echo COMPILATION SUCCESS!
echo.
pause
