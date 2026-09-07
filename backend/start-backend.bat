@echo off
title CypherVault Spring Boot Backend
echo ===================================================
echo   STARTING CYPHERVAULT SPRING BOOT BACKEND SERVER
echo ===================================================
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1"
pause
