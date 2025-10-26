# 🎮 Game For Devs

> **Una aplicación web interactiva de juegos diseñada especialmente para desarrolladores**

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)
![Maven](https://img.shields.io/badge/Maven-Latest-blue)
![Docker](https://img.shields.io/badge/Docker-Supported-blue)
![MariaDB](https://img.shields.io/badge/MariaDB-10.11-red)

## 📋 Descripción del Proyecto

**Game For Devs** es una aplicación web desarrollada con Spring Boot que ofrece una experiencia de juego interactiva para desarrolladores. La aplicación incluye funcionalidades de administración, gestión de usuarios, seguimiento de sesiones de juego y estadísticas detalladas.

### ✨ Características Principales

- 🎯 **Sistema de Juegos Interactivos**: Juegos web diseñados para desarrolladores
- 👥 **Gestión de Usuarios**: Sistema completo de autenticación y autorización
- 📊 **Panel de Administración**: Dashboard para administradores con estadísticas
- 🔍 **Auditoría**: Sistema de logs y seguimiento de actividades
- 📈 **Estadísticas**: Análisis detallado de sesiones de juego
- 🎵 **Gestión de Tracks**: Sistema de pistas musicales para el juego
- 🐳 **Dockerizado**: Despliegue fácil con Docker y Docker Compose

## 🏗️ Arquitectura Técnica

### Stack Tecnológico

- **Backend**: Spring Boot 3.5.7 (Java 17)
- **Base de Datos**: MariaDB 10.11 / H2 (desarrollo)
- **Frontend**: Thymeleaf + HTML/CSS/JavaScript
- **Seguridad**: Spring Security
- **ORM**: Spring Data JPA
- **Contenedores**: Docker & Docker Compose
- **Build**: Maven

### Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/umg/game_for_devs/
│   │   ├── GameForDevsApplication.java          # Clase principal
│   │   ├── config/
│   │   │   ├── DataLoader.java                  # Carga de datos inicial
│   │   │   └── SecurityConfig.java              # Configuración de seguridad
│   │   ├── controller/
│   │   │   ├── AdminController.java             # Controlador del panel admin
│   │   │   ├── AuthController.java              # Controlador de autenticación
│   │   │   ├── GameController.java              # Controlador principal del juego
│   │   │   └── api/GameApiController.java       # API REST del juego
│   │   ├── entity/
│   │   │   ├── User.java                        # Entidad de usuario
│   │   │   ├── GameSession.java                 # Entidad de sesión de juego
│   │   │   ├── Track.java                       # Entidad de pista musical
│   │   │   └── AuditLog.java                    # Entidad de auditoría
│   │   ├── service/
│   │   │   ├── UserService.java                 # Servicio de usuarios
│   │   │   ├── TrackService.java                # Servicio de pistas
│   │   │   ├── StatisticsService.java           # Servicio de estadísticas
│   │   │   └── AuditService.java                # Servicio de auditoría
│   │   └── repository/                          # Repositorios JPA
│   └── resources/
│       ├── templates/                           # Plantillas Thymeleaf
│       ├── static/                              # Recursos estáticos
│       └── application*.properties              # Configuraciones por perfil
```

## 🚀 Inicio Rápido

### Prerrequisitos

- ☕ **Java 17** o superior
- 🔧 **Maven 3.6+**
- 🐳 **Docker & Docker Compose** (opcional, pero recomendado)

### Opción 1: Despliegue con Docker (Recomendado)

```bash
# Clonar el repositorio
git clone <repository-url>
cd game-for-devs

# Windows - Despliegue inicial completo
.\deploy.bat

# Linux/macOS - Configuración inicial y despliegue
# ⚠️ IMPORTANTE: Configurar permisos primero
bash setup-linux.sh    # Configura permisos automáticamente
./deploy.sh             # Despliegue inicial

# Para actualizaciones posteriores (más rápido)
.\update.bat     # Windows
./update.sh      # Linux/macOS (ya tiene permisos)
```

### Opción 2: Ejecución Local

```bash
# Clonar el repositorio
git clone <repository-url>
cd game-for-devs

# Compilar y ejecutar
./mvnw spring-boot:run

# O usando Maven instalado
mvn spring-boot:run
```

### Opción 3: Usando Perfiles Específicos

```bash
# Perfil de desarrollo
run-profile.bat dev

# Perfil de producción
run-profile.bat prod

# Perfil para Docker
run-profile.bat docker
```

## 🌐 Acceso a la Aplicación

Una vez iniciada la aplicación, estará disponible en:

- **Aplicación Principal**: [http://localhost:8080](http://localhost:8080)
- **Panel de Administración**: [http://localhost:8080/admin](http://localhost:8080/admin)
- **API REST**: [http://localhost:8080/api](http://localhost:8080/api)

### 🔐 Credenciales por Defecto

- **Usuario Administrador**: `admin`
- **Contraseña**: `admin123`

## 📊 Funcionalidades

### Para Jugadores
- 🎮 Acceso al juego interactivo
- 📈 Visualización de estadísticas personales
- 🎵 Interacción con pistas musicales durante el juego

### Para Administradores
- 👥 Gestión completa de usuarios
- 📊 Dashboard con estadísticas del sistema
- 🎵 Administración de pistas musicales
- 🔍 Visualización de logs de auditoría
- 📈 Análisis de sesiones de juego

## 🗄️ Base de Datos

### Perfiles de Base de Datos

- **Desarrollo** (`dev`): Base de datos H2 en memoria
- **Docker** (`docker`): MariaDB en contenedor
- **Producción** (`prod`): MariaDB externa

### Esquema Principal

```sql
-- Entidades principales
Users           # Gestión de usuarios del sistema
GameSessions    # Registro de sesiones de juego
Tracks          # Pistas musicales para el juego
AuditLogs       # Logs de auditoría del sistema
```

## 🐳 Docker

### Servicios Incluidos

- **game-for-devs-app**: Aplicación Spring Boot
- **mariadb**: Base de datos MariaDB 10.11
- **Volumes**: Persistencia de datos de la BD

### Comandos Docker Útiles

```bash
# Scripts automatizados (recomendado)
.\deploy.bat          # Despliegue completo
.\update.bat          # Actualización inteligente
.\test-deployment.bat # Verificar despliegue

# Comandos manuales
# Ver logs de la aplicación
docker-compose logs -f app

# Ver logs de la base de datos
docker-compose logs -f mariadb

# Reiniciar servicios
docker-compose restart

# Detener todo
docker-compose down

# Limpiar volúmenes (⚠️ elimina datos)
docker-compose down -v

# Verificar salud de servicios
docker-compose ps
curl http://localhost:8080/actuator/health
```

## 📁 Archivos de Configuración Importantes

- `application.properties`: Configuración base
- `application-dev.properties`: Configuración para desarrollo
- `application-docker.properties`: Configuración para Docker
- `application-prod.properties`: Configuración para producción
- `docker-compose.yml`: Orquestación de contenedores
- `Dockerfile`: Imagen de la aplicación

## 🧪 Testing

```bash
# Ejecutar tests
./mvnw test

# Tests con perfil específico
./mvnw test -Dspring.profiles.active=test
```

## 📋 Scripts Disponibles

### Scripts de Despliegue y Actualización

#### Configuración Inicial
- `setup-linux.sh`: **[NUEVO]** Configuración automática de permisos en Linux/macOS

#### Despliegue Inicial
- `deploy.bat` / `deploy.sh`: Despliegue completo con Docker (incluye git pull automático)
- `test-deployment.bat`: Probar el despliegue y validar servicios

#### Actualización en Producción
- `update.bat` / `update.sh`: **[NUEVO]** Actualización inteligente con validaciones y rollback
- `run-profile.bat`: Ejecutar con perfil específico
- `monitor.bat`: Monitorear logs de la aplicación

### ⚙️ Configuración de Permisos en Linux

**⚠️ IMPORTANTE**: En sistemas Linux/macOS, los scripts `.sh` requieren permisos de ejecución antes de poder ejecutarse.

#### Opción 1: Script Automático (Recomendado)
```bash
# Configuración automática con un solo comando
bash setup-linux.sh
```

#### Opción 2: Configuración Manual
```bash
# Dar permisos a todos los scripts de una vez
chmod +x *.sh

# O dar permisos individualmente
chmod +x deploy.sh
chmod +x update.sh
chmod +x setup-linux.sh

# Verificar permisos (opcional)
ls -la *.sh
```

**💡 Tip**: Una vez que des permisos, no necesitas hacerlo nuevamente a menos que clones el repositorio en una nueva ubicación.

### 🔄 Actualización del Proyecto en Producción

#### Opción 1: Despliegue Completo (Recomendado para primera instalación)
```bash
# Windows
.\deploy.bat

# Linux/macOS
./deploy.sh
```

**Proceso automatizado:**
1. **Git Pull** - Obtiene últimos cambios del repositorio
2. **Detener servicios** - Para contenedores existentes
3. **Limpiar imágenes** - Elimina versiones anteriores
4. **Construir y levantar** - Crea nueva imagen y servicios
5. **Verificar estado** - Confirma que todo funciona

#### Opción 2: Actualización Inteligente (Recomendado para actualizaciones)
```bash
# Windows
.\update.bat

# Linux/macOS
# ⚠️ Si es la primera vez, dar permisos:
chmod +x update.sh
./update.sh
```

**Características avanzadas:**
- ✅ **Verificación Git**: Confirma estado del repositorio
- ✅ **Confirmación interactiva**: Te pregunta antes de hacer cambios
- ✅ **Git Pull con validación**: Descarga cambios con manejo de errores
- ✅ **Backup automático**: Guarda imagen actual por seguridad
- ✅ **Menor downtime**: Mantiene base de datos activa
- ✅ **Health Check**: Verifica que la aplicación funciona correctamente
- ✅ **Rollback automático**: Restaura versión anterior si hay fallos

#### Manejo de Errores en Actualización

**Si hay conflictos Git:**
```bash
git status                    # Ver estado actual
git stash                     # Guardar cambios locales temporalmente
git pull                      # Actualizar desde repositorio
git stash pop                 # Restaurar cambios locales si es necesario
```

**Si la actualización falla:**
```bash
# Rollback manual de emergencia
docker tag game-for-devs-app:backup game-for-devs-app:latest
docker-compose up -d app
```

#### Verificación Post-Actualización
```bash
# Ver estado de servicios
docker-compose ps

# Verificar salud de la aplicación
curl http://localhost:8080/actuator/health

# Monitorear logs en tiempo real
docker-compose logs -f app
```

### Flujo Recomendado para Producción

1. **Desarrollo**: Hacer cambios y commits en rama de desarrollo
2. **Testing**: Probar cambios en entorno local
3. **Merge**: Fusionar cambios a rama principal (main/master)
4. **Deploy**: Ejecutar script de actualización en servidor

```bash
# En el servidor de producción
cd /ruta/al/proyecto

# Windows
.\update.bat

# Linux (primera vez dar permisos)
chmod +x *.sh    # Solo si es la primera vez
./update.sh
```

## 🔧 Desarrollo

### Configuración del Entorno de Desarrollo

1. Clonar el repositorio
2. Importar en tu IDE favorito como proyecto Maven
3. Configurar perfil de desarrollo (`dev`)
4. Ejecutar `GameForDevsApplication.java`

### Hot Reload

La aplicación incluye Spring DevTools para recarga automática durante el desarrollo.

### Generación de Diagramas ER

```bash
# Generar diagrama de entidad-relación
java -cp target/classes com.umg.game_for_devs.util.ERDiagramGenerator
```

## 📈 Monitoring

### Health Checks

- **Health Endpoint**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Info Endpoint**: [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)

### Logs

Los logs se pueden monitorear usando:
```bash
# Aplicación local
tail -f logs/application.log

# Docker
docker-compose logs -f app
```

## 🤝 Contribución

### Flujo de Desarrollo
1. **Fork** del proyecto
2. **Clonar** tu fork localmente
3. **Crear rama** de feature (`git checkout -b feature/AmazingFeature`)
4. **Desarrollar** y probar cambios localmente
5. **Commit** de cambios (`git commit -m 'Add some AmazingFeature'`)
6. **Push** a tu fork (`git push origin feature/AmazingFeature`)
7. **Crear Pull Request** hacia la rama principal

### Testing Local
```bash
# Probar cambios en desarrollo
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Probar con Docker (entorno similar a producción)
.\deploy.bat
```

### Despliegue en Producción
Una vez que los cambios sean aprobados y fusionados:
```bash
# En el servidor de producción (Linux)
cd /ruta/al/proyecto
chmod +x *.sh      # Solo si es la primera vez
./update.sh        # Actualización automática con git pull

# En servidor Windows
cd C:\ruta\al\proyecto
.\update.bat       # Actualización automática con git pull
```

## 📄 Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).

## 📞 Soporte

Para reportar bugs o solicitar nuevas funcionalidades, por favor crear un issue en el repositorio.

---

**Desarrollado con ❤️ para la comunidad de desarrolladores**