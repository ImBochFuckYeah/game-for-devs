@echo off
REM Script optimizado para actualizar Game For Devs en producción
REM Incluye git pull, verificaciones y rollback automático en caso de fallo

echo ======================================
echo  Game For Devs - Actualización
echo ======================================

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
docker-compose ps

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
docker-compose stop app

echo.
echo [6/8] Construyendo nueva versión...
docker-compose build app
if errorlevel 1 (
    echo ERROR: Fallo en construcción, restaurando servicio anterior...
    docker-compose start app
    pause
    exit /b 1
)

echo.
echo [7/8] Levantando nueva versión...
docker-compose up -d app

echo.
echo [8/8] Verificando salud de la aplicación...
timeout /t 30 /nobreak > nul
echo Probando endpoint de salud...
curl -f http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo WARNING: Aplicación no responde, verificar logs...
    docker-compose logs --tail=50 app
    echo.
    set /p rollback="¿Realizar rollback? (s/n): "
    if /i "%rollback%"=="s" (
        echo Realizando rollback...
        docker-compose stop app
        docker tag game-for-devs-app:backup game-for-devs-app:latest
        docker-compose up -d app
        echo Rollback completado
    )
) else (
    echo ✓ Aplicación funcionando correctamente
    echo ✓ Actualización exitosa!
)

echo.
echo Estado final de servicios...
docker-compose ps

echo.
echo ======================================
echo  Actualización completada!
echo ======================================
echo.
echo URLs disponibles:
echo - Aplicación: http://localhost:8080
echo - Health Check: http://localhost:8080/actuator/health
echo.
echo Para ver logs: docker-compose logs -f app
echo Para rollback manual: docker tag game-for-devs-app:backup game-for-devs-app:latest
echo ======================================