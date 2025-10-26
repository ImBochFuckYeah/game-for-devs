@echo off
REM Script para desplegar Game For Devs con Docker Compose
REM Ejecutar desde el directorio raíz del proyecto

echo ======================================
echo  Game For Devs - Docker Deployment
echo ======================================

echo.
echo [1/5] Actualizando código desde repositorio...
git pull
if errorlevel 1 (
    echo ERROR: No se pudo actualizar el código desde el repositorio
    echo Verifique su conexión a internet y permisos de Git
    pause
    exit /b 1
)

echo.
echo [2/5] Deteniendo contenedores existentes...
docker-compose down

echo.
echo [3/5] Limpiando imágenes antiguas...
docker rmi game-for-devs-app:latest 2>nul

echo.
echo [4/5] Construyendo y levantando servicios...
docker-compose up --build -d

echo.
echo [5/5] Verificando estado de los servicios...
timeout /t 10 /nobreak > nul
docker-compose ps

echo.
echo ======================================
echo  Despliegue completado!
echo ======================================
echo.
echo Servicios disponibles:
echo - Aplicación: http://localhost:8080
echo - Adminer (DB Admin): http://localhost:8081
echo   * Servidor: mariadb
echo   * Usuario: spring
echo   * Contraseña: Sefgag$$2024
echo   * Base de datos: game_for_devs
echo.
echo Para ver los logs: docker-compose logs -f
echo Para detener: docker-compose down
echo ======================================