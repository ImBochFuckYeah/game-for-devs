@echo off
echo ============================================
echo    Game For Devs - Selector de Perfiles
echo ============================================
echo.
echo Seleccione el perfil a usar:
echo.
echo 1. Desarrollo (H2 - Base de datos en memoria)
echo 2. Produccion (MariaDB - Base de datos persistente)
echo 3. Ver perfil actual
echo 4. Salir
echo.

set /p choice="Ingrese su opcion (1-4): "

if "%choice%"=="1" (
    echo.
    echo Iniciando en modo DESARROLLO...
    echo - Base de datos: H2 en memoria
    echo - Consola H2: http://localhost:8080/h2-console
    echo - Logs: Modo DEBUG activado
    echo - Datos: Carga automatica de datos de prueba
    echo.
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    pause
) else if "%choice%"=="2" (
    echo.
    echo Iniciando en modo PRODUCCION...
    echo - Base de datos: MariaDB
    echo - Logs: Modo INFO/WARN
    echo - IMPORTANTE: Asegurese de que MariaDB este ejecutandose
    echo.
    set /p confirm="¿Esta MariaDB ejecutandose? (y/n): "
    if /i "%confirm%"=="y" (
        mvn spring-boot:run -Dspring-boot.run.profiles=prod
    ) else (
        echo Inicie MariaDB primero y vuelva a intentar.
    )
    pause
) else if "%choice%"=="3" (
    echo.
    echo Verificando configuracion actual...
    findstr "spring.profiles.active" src\main\resources\application.properties
    echo.
    pause
    goto :start
) else if "%choice%"=="4" (
    echo Saliendo...
    exit /b 0
) else (
    echo Opcion invalida. Intente de nuevo.
    pause
    goto :start
)

:start
goto :eof