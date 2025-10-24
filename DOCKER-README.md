# 🐳 Game For Devs - Despliegue con Docker

Este documento explica cómo desplegar la aplicación **Game For Devs** usando Docker y Docker Compose.

## 📋 Prerrequisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y funcionando
- [Docker Compose](https://docs.docker.com/compose/) (incluido con Docker Desktop)
- Al menos 2GB de RAM libre
- Puertos 8080, 8081 y 3306 disponibles

## 🚀 Despliegue Rápido

### Windows
```cmd
deploy.bat
```

### Linux/macOS
```bash
chmod +x deploy.sh
./deploy.sh
```

### Manual
```bash
docker-compose up --build -d
```

## 🏗️ Arquitectura del Despliegue

El despliegue incluye 3 contenedores:

### 1. **game-for-devs-app** - Aplicación Spring Boot
- **Puerto**: 8080
- **Imagen**: Construida localmente desde Dockerfile
- **Tecnologías**: OpenJDK 17, Spring Boot 3.x, Maven
- **Perfil**: `docker`

### 2. **game-for-devs-db** - Base de Datos MariaDB
- **Puerto**: 3306
- **Imagen**: `mariadb:10.11`
- **Credenciales**:
  - Usuario: `spring`
  - Contraseña: `Sefgag$$2024`
  - Base de datos: `game_for_devs`

### 3. **game-for-devs-adminer** - Administrador de BD (Opcional)
- **Puerto**: 8081
- **Imagen**: `adminer:4.8.1`
- **Uso**: Interfaz web para administrar la base de datos

## 🌐 Acceso a la Aplicación

Una vez desplegado, accede a:

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Aplicación Principal** | http://localhost:8080 | admin / admin123 |
| **Administrador de BD** | http://localhost:8081 | spring / Sefgag$$2024 |

## 📁 Estructura de Archivos Docker

```
├── Dockerfile                      # Imagen de la aplicación Spring Boot
├── docker-compose.yml            # Orquestación de contenedores
├── .dockerignore                  # Archivos excluidos del build
├── init-db.sql                    # Script de inicialización de BD
├── deploy.bat                     # Script de despliegue (Windows)
├── deploy.sh                      # Script de despliegue (Linux/macOS)
└── src/main/resources/
    └── application-docker.properties  # Configuración para Docker
```

## ⚙️ Configuración Avanzada

### Variables de Entorno

Puedes personalizar la configuración modificando las variables de entorno en `docker-compose.yml`:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - SPRING_DATASOURCE_URL=jdbc:mariadb://mariadb:3306/game_for_devs
  - SPRING_DATASOURCE_USERNAME=spring
  - SPRING_DATASOURCE_PASSWORD=Sefgag$$2024
  - JAVA_OPTS=-Xmx512m -Xms256m
```

### Persistencia de Datos

Los datos de la base de datos se almacenan en un volumen Docker persistente llamado `mariadb_data`. Esto asegura que los datos no se pierdan al reiniciar los contenedores.

### Health Checks

Ambos servicios incluyen health checks:
- **MariaDB**: Verifica la conectividad e inicialización de InnoDB
- **Spring Boot**: Verifica el endpoint `/actuator/health`

## 🔧 Comandos Útiles

### Ver logs en tiempo real
```bash
docker-compose logs -f
```

### Ver logs de un servicio específico
```bash
docker-compose logs -f app
docker-compose logs -f mariadb
```

### Verificar estado de los servicios
```bash
docker-compose ps
```

### Reiniciar un servicio
```bash
docker-compose restart app
```

### Detener todos los servicios
```bash
docker-compose down
```

### Detener y eliminar volúmenes (⚠️ Elimina datos de BD)
```bash
docker-compose down -v
```

### Reconstruir sin caché
```bash
docker-compose build --no-cache
docker-compose up -d
```

## 🛠️ Desarrollo y Debugging

### Modo Desarrollo
Para desarrollo activo, puedes montar el código fuente:

```yaml
services:
  app:
    volumes:
      - ./src:/app/src
      - ./target:/app/target
```

### Acceso Directo al Contenedor
```bash
# Acceder al contenedor de la aplicación
docker exec -it game-for-devs-app sh

# Acceder al contenedor de base de datos
docker exec -it game-for-devs-db mysql -u spring -p
```

## 🚨 Solución de Problemas

### Puerto ya en uso
Si el puerto 8080 está ocupado:
```bash
# Cambiar puerto en docker-compose.yml
ports:
  - "8090:8080"  # Cambia 8090 por el puerto deseado
```

### Problemas de memoria
Si la aplicación se queda sin memoria:
```yaml
environment:
  - JAVA_OPTS=-Xmx1024m -Xms512m  # Aumentar memoria
```

### Base de datos no se conecta
Verifica que MariaDB esté completamente inicializada:
```bash
docker-compose logs mariadb
```

### Limpiar todo y empezar de nuevo
```bash
docker-compose down -v
docker system prune -f
docker-compose up --build -d
```

## 📈 Producción

Para un entorno de producción, considera:

1. **Usar secretos** en lugar de variables de entorno para contraseñas
2. **Configurar HTTPS** con un proxy reverso (nginx)
3. **Usar imágenes específicas** en lugar de `latest`
4. **Configurar backups** automáticos de la base de datos
5. **Monitoreo** con herramientas como Prometheus/Grafana
6. **Logs centralizados** con ELK stack o similar

### Ejemplo de configuración de producción:
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    image: game-for-devs:1.0.0
    restart: always
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

## 📚 Referencias

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [MariaDB Docker Hub](https://hub.docker.com/_/mariadb)