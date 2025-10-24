@echo off
REM Script para monitorear el estado de los servicios de Game For Devs
REM Muestra información en tiempo real sobre el estado de los contenedores

echo ======================================
echo  Game For Devs - Monitor de Servicios
echo ======================================

:loop
cls
echo ======================================
echo  Game For Devs - Monitor de Servicios
echo ======================================
echo Fecha/Hora: %date% %time%
echo.

echo [Estado de los Servicios]
docker-compose ps

echo.
echo [Uso de Recursos]
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

echo.
echo [Health Checks]
docker inspect game-for-devs-app --format="App Health: {{.State.Health.Status}}" 2>nul
docker inspect game-for-devs-db --format="DB Health: {{.State.Health.Status}}" 2>nul

echo.
echo [Logs Recientes - App]
docker-compose logs --tail=3 app 2>nul

echo.
echo [Logs Recientes - DB]
docker-compose logs --tail=2 mariadb 2>nul

echo.
echo ======================================
echo Presiona Ctrl+C para salir
echo Actualizando en 10 segundos...
echo ======================================

timeout /t 10 /nobreak > nul
goto loop