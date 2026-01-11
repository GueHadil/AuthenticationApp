@echo off
REM ==============================================
REM CloudDesk User Manager - SMTP Setup and Run
REM Usage: Double-click or run in CMD:
REM   setup_and_run_smtp.bat
REM Requirements:
REM - Place jakarta.mail-2.0.1.jar in the libs folder
REM - Source code under src\com\example\
REM - Java JDK installed and in PATH (javac/java)
REM ==============================================

REM ----- SMTP configuration (Gmail) -----
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=guellazhadil20@gmail.com
set MAIL_PASSWORD=hadildada
set MAIL_FROM=guellazhadil20@gmail.com
set MAIL_STARTTLS=true
set MAIL_SSL=false
set MAIL_DEBUG=false

REM ----- Project paths -----
set SRC_DIR=src\com\example
set OUT_DIR=out
set LIBS_DIR=libs
set JAKARTA_MAIL_JAR=%LIBS_DIR%\jakarta.mail-2.0.1.jar

REM ----- Check jakarta mail jar -----
if not exist "%JAKARTA_MAIL_JAR%" (
    echo [ERROR] Jakarta Mail JAR not found: %JAKARTA_MAIL_JAR%
    echo Download it from:
    echo https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar
    echo and place it in the "libs" folder.
    pause
    exit /b 1
)

REM ----- Create output directory -----
if not exist "%OUT_DIR%" (
    mkdir "%OUT_DIR%"
)

REM ----- Compile sources -----
echo [INFO] Compiling sources...
javac -cp "%JAKARTA_MAIL_JAR%" -d "%OUT_DIR%" "%SRC_DIR%\Mailer.java" "%SRC_DIR%\ConsoleMailer.java" "%SRC_DIR%\SmtpMailer.java" "%SRC_DIR%\EmailTemplates.java" "%SRC_DIR%\UserService.java" "%SRC_DIR%\UserDAO.java" "%SRC_DIR%\DatabaseConnection.java" "%SRC_DIR%\User.java" "%SRC_DIR%\UserManagementGUI.java"
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

REM ----- Run application -----
echo [INFO] Running application with SMTP configuration...
echo   MAIL_HOST=%MAIL_HOST%
echo   MAIL_PORT=%MAIL_PORT%
echo   MAIL_USERNAME=%MAIL_USERNAME%
echo   MAIL_FROM=%MAIL_FROM%
echo   MAIL_STARTTLS=%MAIL_STARTTLS%
echo   MAIL_SSL=%MAIL_SSL%
echo   MAIL_DEBUG=%MAIL_DEBUG%

java -cp "%OUT_DIR%;%JAKARTA_MAIL_JAR%" com.example.UserManagementGUI
if errorlevel 1 (
    echo [ERROR] Application failed to start.
    pause
    exit /b 1
)

echo [INFO] Application exited.
pause