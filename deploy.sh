#!/bin/bash
# Script para desplegar Game For Devs con Docker Compose
# Ejecutar desde el directorio raíz del proyecto

# Función para detectar comando de Docker Compose
detect_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        echo "docker-compose"
    elif docker compose version &> /dev/null; then
        echo "docker compose"
    else
        echo "ERROR: Docker Compose no está instalado"
        exit 1
    fi
}

# Detectar comando de Docker Compose
DOCKER_COMPOSE=$(detect_docker_compose)

echo "======================================"
echo " Game For Devs - Docker Deployment"
echo "======================================"
echo "Usando: $DOCKER_COMPOSE"

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
$DOCKER_COMPOSE down

echo ""
echo "[3/5] Limpiando imágenes antiguas..."
docker rmi game-for-devs-app:latest 2>/dev/null || true

echo ""
echo "[4/5] Construyendo y levantando servicios..."
$DOCKER_COMPOSE up --build -d

echo ""
echo "[5/5] Verificando estado de los servicios..."
sleep 10
$DOCKER_COMPOSE ps

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
echo "Para ver los logs: $DOCKER_COMPOSE logs -f"
echo "Para detener: $DOCKER_COMPOSE down"
echo "======================================"