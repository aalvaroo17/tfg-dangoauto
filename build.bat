@echo off
REM Script de compilación para Windows

echo ==========================================
echo Compilando DangoAuto
echo ==========================================
echo.

REM Verificar Maven - Intentar ubicación local primero
set "MAVEN_HOME=%USERPROFILE%\Apache\Maven"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    REM Si no está en ubicación local, buscar en PATH
    where mvn >nul 2>&1
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Maven no esta instalado
        echo.
        echo Para instalar Maven automaticamente:
        echo   powershell -ExecutionPolicy Bypass -File install-maven.ps1
        echo.
        echo O instala manualmente desde: https://maven.apache.org/download.cgi
        echo.
        pause
        exit /b 1
    )
    set "MAVEN_CMD=mvn"
) else (
    echo Usando Maven desde: %MAVEN_HOME%
)

REM Compilar aplicación Java
echo Compilando aplicación Java...
cd java-app
call "%MAVEN_CMD%" clean package

if %errorlevel% equ 0 (
    echo.
    echo Copiando JAR a dist/...
    copy target\dangoauto-app-1.0.0.jar ..\dist\DangoAuto.jar
    echo ¡Compilación exitosa!
    echo JAR disponible en: dist\DangoAuto.jar
) else (
    echo Error en la compilación
    exit /b 1
)

cd ..

pause

