@echo off
REM Script optimizado para actualizar Game For Devs en producción
REM Incluye git pull, verificaciones y rollback automático en caso de fallo

REM Detectar comando de Docker Compose
docker-compose version >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Docker Compose no está instalado
        pause
        exit /b 1
    ) else (
        set DOCKER_COMPOSE=docker compose
    )
) else (
    set DOCKER_COMPOSE=docker-compose
)

echo ======================================
echo  Game For Devs - Actualización
echo ======================================
echo Usando: %DOCKER_COMPOSE%

echo.
echo [1/8] Verificando repositorio Git...
git status >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se encuentra repositorio Git o Git no está instalado
    pause
    exit /b 1
)

echo.
echo [2/8] Verificando estado actual...
%DOCKER_COMPOSE% ps

echo.
echo [3/8] Obteniendo últimos cambios del repositorio...
git fetch
git status
set /p confirm="¿Continuar con git pull? (s/n): "
if /i not "%confirm%"=="s" (
    echo Actualización cancelada por el usuario
    pause
    exit /b 0
)

git pull
if errorlevel 1 (
    echo ERROR: No se pudo actualizar el código desde el repositorio
    echo Verifique conflictos o permisos de Git
    pause
    exit /b 1
)

echo.
echo [4/8] Creando backup de imagen actual...
docker tag game-for-devs-app:latest game-for-devs-app:backup 2>nul

echo.
echo [5/8] Deteniendo aplicación (manteniendo DB)...
%DOCKER_COMPOSE% stop app

echo.
echo [6/8] Construyendo nueva versión...
%DOCKER_COMPOSE% build app
if errorlevel 1 (
    echo ERROR: Fallo en construcción, restaurando servicio anterior...
    %DOCKER_COMPOSE% start app
    pause
    exit /b 1
)

echo.
echo [7/8] Levantando nueva versión...
%DOCKER_COMPOSE% up -d app

echo.
echo [8/8] Verificando salud de la aplicación...
timeout /t 30 /nobreak > nul
echo Probando endpoint de salud...
curl -f http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo WARNING: Aplicación no responde, verificar logs...
    %DOCKER_COMPOSE% logs --tail=50 app
    echo.
    set /p rollback="¿Realizar rollback? (s/n): "
    if /i "%rollback%"=="s" (
        echo Realizando rollback...
        %DOCKER_COMPOSE% stop app
        docker tag game-for-devs-app:backup game-for-devs-app:latest
        %DOCKER_COMPOSE% up -d app
        echo Rollback completado
    )
) else (
    echo ✓ Aplicación funcionando correctamente
    echo ✓ Actualización exitosa!
)

echo.
echo Estado final de servicios...
%DOCKER_COMPOSE% ps

echo.
echo ======================================
echo  Actualización completada!
echo ======================================
echo.
echo URLs disponibles:
echo - Aplicación: http://localhost:8080
echo - Health Check: http://localhost:8080/actuator/health
echo.
echo Para ver logs: %DOCKER_COMPOSE% logs -f app
echo Para rollback manual: docker tag game-for-devs-app:backup game-for-devs-app:latest
echo ======================================