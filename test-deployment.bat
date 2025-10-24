@echo off
REM Script para probar el despliegue de Game For Devs con Docker
REM Verifica que todos los servicios estén funcionando correctamente

echo ======================================
echo  Game For Devs - Test de Despliegue
echo ======================================

echo.
echo [1/6] Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker no está instalado o no está funcionando
    pause
    exit /b 1
) else (
    echo ✓ Docker está disponible
)

echo.
echo [2/6] Verificando Docker Compose...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker Compose no está instalado
    pause
    exit /b 1
) else (
    echo ✓ Docker Compose está disponible
)

echo.
echo [3/6] Verificando puertos disponibles...
netstat -an | findstr :8080 >nul 2>&1
if not errorlevel 1 (
    echo WARNING: Puerto 8080 está ocupado
) else (
    echo ✓ Puerto 8080 disponible
)

netstat -an | findstr :3306 >nul 2>&1
if not errorlevel 1 (
    echo WARNING: Puerto 3306 está ocupado
) else (
    echo ✓ Puerto 3306 disponible
)

netstat -an | findstr :8081 >nul 2>&1
if not errorlevel 1 (
    echo WARNING: Puerto 8081 está ocupado
) else (
    echo ✓ Puerto 8081 disponible
)

echo.
echo [4/6] Verificando archivos necesarios...
if exist "Dockerfile" (
    echo ✓ Dockerfile encontrado
) else (
    echo ERROR: Dockerfile no encontrado
    pause
    exit /b 1
)

if exist "docker-compose.yml" (
    echo ✓ docker-compose.yml encontrado
) else (
    echo ERROR: docker-compose.yml no encontrado
    pause
    exit /b 1
)

if exist "pom.xml" (
    echo ✓ pom.xml encontrado
) else (
    echo ERROR: pom.xml no encontrado
    pause
    exit /b 1
)

echo.
echo [5/6] Construyendo imagen de prueba...
docker build -t game-for-devs-test . --quiet
if errorlevel 1 (
    echo ERROR: No se pudo construir la imagen Docker
    pause
    exit /b 1
) else (
    echo ✓ Imagen construida exitosamente
)

echo.
echo [6/6] Limpiando imagen de prueba...
docker rmi game-for-devs-test >nul 2>&1

echo.
echo ======================================
echo  ✓ Todos los tests pasaron!
echo ======================================
echo.
echo El proyecto está listo para desplegarse.
echo Ejecuta 'deploy.bat' para iniciar el despliegue.
echo.
pause