#!/bin/bash
# Script optimizado para actualizar Game For Devs en producción
# Incluye git pull, verificaciones y rollback automático en caso de fallo

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
echo " Game For Devs - Actualización"
echo "======================================"
echo "Usando: $DOCKER_COMPOSE"

echo ""
echo "[1/8] Verificando repositorio Git..."
if ! git status >/dev/null 2>&1; then
    echo "ERROR: No se encuentra repositorio Git o Git no está instalado"
    exit 1
fi

echo ""
echo "[2/8] Verificando estado actual..."
$DOCKER_COMPOSE ps

echo ""
echo "[3/8] Obteniendo últimos cambios del repositorio..."
git fetch
git status
read -p "¿Continuar con git pull? (s/n): " confirm
if [[ ! "$confirm" =~ ^[SsYy]$ ]]; then
    echo "Actualización cancelada por el usuario"
    exit 0
fi

git pull
if [ $? -ne 0 ]; then
    echo "ERROR: No se pudo actualizar el código desde el repositorio"
    echo "Verifique conflictos o permisos de Git"
    exit 1
fi

echo ""
echo "[4/8] Creando backup de imagen actual..."
docker tag game-for-devs-app:latest game-for-devs-app:backup 2>/dev/null || true

echo ""
echo "[5/8] Deteniendo aplicación (manteniendo DB)..."
$DOCKER_COMPOSE stop app

echo ""
echo "[6/8] Construyendo nueva versión..."
if ! $DOCKER_COMPOSE build app; then
    echo "ERROR: Fallo en construcción, restaurando servicio anterior..."
    $DOCKER_COMPOSE start app
    exit 1
fi

echo ""
echo "[7/8] Levantando nueva versión..."
$DOCKER_COMPOSE up -d app

echo ""
echo "[8/8] Verificando salud de la aplicación..."
sleep 30
echo "Probando endpoint de salud..."
if ! curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "WARNING: Aplicación no responde, verificar logs..."
    $DOCKER_COMPOSE logs --tail=50 app
    echo ""
    read -p "¿Realizar rollback? (s/n): " rollback
    if [[ "$rollback" =~ ^[SsYy]$ ]]; then
        echo "Realizando rollback..."
        $DOCKER_COMPOSE stop app
        docker tag game-for-devs-app:backup game-for-devs-app:latest
        $DOCKER_COMPOSE up -d app
        echo "Rollback completado"
    fi
else
    echo "✓ Aplicación funcionando correctamente"
    echo "✓ Actualización exitosa!"
fi

echo ""
echo "Estado final de servicios..."
$DOCKER_COMPOSE ps

echo ""
echo "======================================"
echo " Actualización completada!"
echo "======================================"
echo ""
echo "URLs disponibles:"
echo "- Aplicación: http://localhost:8080"
echo "- Health Check: http://localhost:8080/actuator/health"
echo ""
echo "Para ver logs: $DOCKER_COMPOSE logs -f app"
echo "Para rollback manual: docker tag game-for-devs-app:backup game-for-devs-app:latest"
echo "======================================"