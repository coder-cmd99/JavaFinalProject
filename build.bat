@echo off
:: ============================================================
:: Brew & Co. POS — Windows Build & Run Script
:: ============================================================
:: Requirements:
::   - JDK 17+ on PATH
::   - MySQL JDBC driver in .\lib\  (mysql-connector-j-*.jar)
::   - MySQL running with the schema loaded (run schema.sql first)
:: ============================================================

setlocal enabledelayedexpansion

set APP_NAME=CafePOS
set SRC_ROOT=src\main\java
set OUT_DIR=out
set LIB_DIR=lib
set MAIN_CLASS=com.cafepos.Main

:: ── Find JDBC jar ──────────────────────────────────────────
set JDBC_JAR=
for %%f in (%LIB_DIR%\mysql-connector-j-*.jar) do set JDBC_JAR=%%f

if "%JDBC_JAR%"=="" (
    echo.
    echo  ERROR: No mysql-connector-j-*.jar found in .\lib\
    echo  Download from: https://dev.mysql.com/downloads/connector/j/
    echo  Place the JAR in the .\lib\ folder and re-run.
    echo.
    pause & exit /b 1
)

echo Using JDBC driver: %JDBC_JAR%

:: ── Compile ────────────────────────────────────────────────
if not exist %OUT_DIR% mkdir %OUT_DIR%

echo Compiling…
dir /s /b %SRC_ROOT%\*.java > sources.txt
javac -cp "%JDBC_JAR%" -d %OUT_DIR% @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo.
    echo  Compilation failed. Check errors above.
    echo.
    pause & exit /b 1
)

echo Build successful!
echo.

:: ── Run ────────────────────────────────────────────────────
echo Starting %APP_NAME%…
java -cp "%OUT_DIR%;%JDBC_JAR%" %MAIN_CLASS%

endlocal
