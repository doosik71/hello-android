@echo off
setlocal

set "ADB=%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe"
set "APK=app\build\outputs\apk\debug\app-debug.apk"
set "DEVICE=emulator-5554"

:LOOP
echo ===============================
echo [1] Starting adb server...
echo ===============================
call "%ADB%" start-server >nul 2>&1

echo ===============================
echo [2] Checking device state...
echo ===============================
set "STATE="
for /f "delims=" %%A in ('call "%ADB%" -s %DEVICE% get-state 2^>nul') do set "STATE=%%A"

echo Current state: [%STATE%]

if /i not "%STATE%"=="device" goto WAIT

echo.
echo [3] Device ready. Trying install...
echo ===============================

call "%ADB%" -s %DEVICE% install --no-streaming -r "%APK%" > "%TEMP%\install_result.txt" 2>&1
type "%TEMP%\install_result.txt"

findstr /C:"Success" "%TEMP%\install_result.txt" >nul 2>&1
if not errorlevel 1 goto SUCCESS

echo.
echo Install failed. Retrying...

:WAIT
echo.
echo Waiting 2 seconds...
timeout /t 2 >nul
goto LOOP

:SUCCESS
echo.
echo INSTALL SUCCESS!
del "%TEMP%\install_result.txt" >nul 2>&1
pause