@echo off
echo ================================================================================
echo           CYBERSENTINEL SOC PLATFORM — END-TO-END ACCEPTANCE TEST
echo ================================================================================
echo.

call build.bat
if %errorlevel% neq 0 exit /b 1

echo.
echo [TESTING] Running 5 SOC Workflows Automated Verification...
java -cp "bin;lib\*" com.cybersentinel.test.SystemVerificationTest

pause
