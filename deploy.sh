#!/bin/bash
# Script para desplegar Game For Devs con Docker Compose
# Ejecutar desde el directorio raíz del proyecto

echo "======================================"
echo " Game For Devs - Docker Deployment"
echo "======================================"

echo ""
echo "[1/5] Actualizando código desde repositorio..."
git pull
if [ $? -ne 0 ]; then
    echo "ERROR: No se pudo actualizar el código desde el repositorio"
    echo "Verifique su conexión a internet y permisos de Git"
    exit 1
fi

echo ""
echo "[2/5] Deteniendo contenedores existentes..."
docker-compose down

echo ""
echo "[3/5] Limpiando imágenes antiguas..."
docker rmi game-for-devs-app:latest 2>/dev/null || true

echo ""
echo "[4/5] Construyendo y levantando servicios..."
docker-compose up --build -d

echo ""
echo "[5/5] Verificando estado de los servicios..."
sleep 10
docker-compose ps

echo ""
echo "======================================"
echo " Despliegue completado!"
echo "======================================"
echo ""
echo "Servicios disponibles:"
echo "- Aplicación: http://localhost:8080"
echo "- Adminer (DB Admin): http://localhost:8081"
echo "  * Servidor: mariadb"
echo "  * Usuario: spring"
echo "  * Contraseña: Sefgag\$\$2024"
echo "  * Base de datos: game_for_devs"
echo ""
echo "Para ver los logs: docker-compose logs -f"
echo "Para detener: docker-compose down"
echo "======================================"