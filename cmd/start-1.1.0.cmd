@echo off

REM Set the window size in the registry for Command Prompt
REM Change the registry key for the default window size
reg add "HKEY_CURRENT_USER\Console" /v WindowSize /t REG_BINARY /d 78002400 /f

REM Launch Java in the current CMD window
"%~dp0runtime\bin\java.exe" -jar "%~dp0tetrad-1.1.0.jar" -PROD
