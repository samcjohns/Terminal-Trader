@echo off

REM Set the window size
mode con: cols=120 lines=36

REM Launch Java in the current CMD window
"%~dp0runtime\bin\java.exe" -jar "%~dp0tetrad-1.0.0.jar" -NDEV