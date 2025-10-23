# 🎮 Game For Devs - Configuración de Perfiles

Este proyecto soporta múltiples perfiles para diferentes entornos de desarrollo y producción, permitiendo una transición fluida entre H2 (desarrollo) y MariaDB (producción).

## 📋 Perfiles Disponibles

### 🔧 Desarrollo (`dev`) - **Perfil por Defecto**
- **Base de datos**: H2 en memoria (jdbc:h2:mem:testdb)
- **Persistencia**: Los datos se reinician en cada ejecución
- **Consola H2**: Disponible en `http://localhost:8080/h2-console`
- **Logs**: Nivel DEBUG activado para desarrollo
- **Datos**: Carga automática desde `data-dev.sql`
- **Rendimiento**: Optimizado para desarrollo rápido

### 🚀 Producción (`prod`)
- **Base de datos**: MariaDB persistente (localhost:3306/game_for_devs)
- **Persistencia**: Los datos se mantienen entre ejecuciones
- **Consola H2**: Deshabilitada por seguridad
- **Logs**: Nivel INFO/WARN para rendimiento
- **Datos**: Configuración manual desde `data-prod.sql`
- **Rendimiento**: Optimizado para producción

## 🚀 Cómo Ejecutar

### Opción 1: Script Automático (Windows) ⭐
```cmd
run-profile.bat
```
*Script interactivo que te guía en la selección del perfil*

### Opción 2: Maven Directo

#### Desarrollo (ejecuta automáticamente por defecto)
```bash
mvn spring-boot:run
```

#### Desarrollo (explícito)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Producción
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Opción 3: Variables de Entorno
```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run

# Windows CMD
set SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

## 🗄️ Configuración de Base de Datos

### H2 (Desarrollo) - Acceso a la Consola
```properties
URL de la aplicación: http://localhost:8080
Consola H2: http://localhost:8080/h2-console

# Credenciales H2
JDBC URL: jdbc:h2:mem:testdb
Driver Class: org.h2.Driver
Usuario: sa
Contraseña: (vacía)
```

### MariaDB (Producción)
```properties
# Configuración requerida en application-prod.properties
URL: jdbc:mariadb://localhost:3306/game_for_devs
Usuario: spring
Contraseña: Sefgag$$2024
Driver: org.mariadb.jdbc.Driver
```

## ⚙️ Configuración Inicial de MariaDB

Para usar el perfil de producción, necesitas configurar MariaDB:

### 1. Crear Base de Datos
```sql
-- Conectarse como administrador
mysql -u root -p

-- Crear base de datos
CREATE DATABASE game_for_devs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario específico
CREATE USER 'spring'@'localhost' IDENTIFIED BY 'Sefgag$$2024';
GRANT ALL PRIVILEGES ON game_for_devs.* TO 'spring'@'localhost';
FLUSH PRIVILEGES;

-- Verificar
SHOW DATABASES;
USE game_for_devs;
```

### 2. Ejecutar Datos Iniciales
```sql
-- Desde MySQL CLI
SOURCE src/main/resources/data-prod.sql;

-- O desde línea de comandos
mysql -u spring -p game_for_devs < src/main/resources/data-prod.sql
```

## 🔄 Cambiar Perfil Activo

### Método 1: Temporalmente (una ejecución)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=PERFIL_DESEADO
```

### Método 2: Permanentemente
Editar `src/main/resources/application.properties`:
```properties
# Cambiar esta línea:
spring.profiles.active=dev  # o prod
```

### Método 3: Variable de Sistema
```bash
# Establecer variable de entorno antes de ejecutar
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

## 📊 Comparación de Características

| Característica | Desarrollo (`dev`) | Producción (`prod`) |
|----------------|-------------------|-------------------|
| **Base de datos** | H2 en memoria | MariaDB persistente |
| **Tiempo de inicio** | ✅ Rápido | 🔶 Medio |
| **Consola H2** | ✅ Habilitada | ❌ Deshabilitada |
| **Logs SQL** | ✅ Visible | ❌ Oculto |
| **Logs de aplicación** | DEBUG | INFO/WARN |
| **Datos iniciales** | ✅ Automático | ⚠️ Manual |
| **Reinicio limpio** | ✅ Siempre | ❌ Persistente |
| **Pool de conexiones** | Básico | Optimizado |
| **Seguridad** | 🔶 Desarrollo | ✅ Producción |
| **Dependencias externas** | ❌ Ninguna | ✅ MariaDB |

## 🛠️ Solución de Problemas

### ❌ Error: No se puede conectar a MariaDB
```bash
# 1. Verificar que MariaDB esté ejecutándose
mysql --version
systemctl status mariadb  # Linux
net start mariadb         # Windows

# 2. Probar conexión manual
mysql -u spring -p -h localhost

# 3. Verificar base de datos
mysql -u spring -p
SHOW DATABASES;
USE game_for_devs;
SHOW TABLES;
```

### ❌ Error: Perfil no reconocido
```bash
# Verificar perfil actual
grep "spring.profiles.active" src/main/resources/application.properties

# Limpiar y recompilar
mvn clean compile

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### ❌ Error 500 en /admin/tracks
```bash
# 1. Verificar que la aplicación use el perfil correcto
# Buscar en los logs: "The following 1 profile is active: dev"

# 2. Para desarrollo (H2)
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Verificar consola H2
# http://localhost:8080/h2-console
```

### ❌ Datos no se cargan automáticamente
```bash
# Para desarrollo
# Verificar que exista: src/main/resources/data-dev.sql
# Verificar en application-dev.properties:
# spring.sql.init.mode=always

# Para producción
# Ejecutar manualmente: data-prod.sql
```

## 📁 Estructura de Archivos de Configuración

```
src/main/resources/
├── application.properties              # ⚙️ Configuración base + perfil por defecto
├── application-dev.properties          # 🔧 Configuración específica de desarrollo  
├── application-prod.properties         # 🚀 Configuración específica de producción
├── data-dev.sql                       # 📊 Datos de prueba para desarrollo (H2)
├── data-prod.sql                      # 📊 Datos iniciales para producción (MariaDB)
└── data.sql                           # 📊 Archivo legacy (no se usa con perfiles)

# Archivos auxiliares
run-profile.bat                        # 🖥️ Script interactivo para Windows
PROFILES-README.md                     # 📖 Esta documentación
```

## 🎯 Recomendaciones de Uso

### 👨‍💻 Para Desarrollo Diario
- **Usar**: Perfil `dev` (por defecto)
- **Ventaja**: Configuración cero, datos limpios en cada reinicio
- **Comando**: `mvn spring-boot:run`

### 🧪 Para Testing Local  
- **Usar**: Perfil `dev`
- **Ventaja**: Datos consistentes, fácil debugging
- **Comando**: `mvn clean spring-boot:run`

### 🎬 Para Demos y Presentaciones
- **Usar**: Perfil `dev`
- **Ventaja**: Configuración simple, sin dependencias externas
- **Comando**: `run-profile.bat` → Opción 1

### 🌐 Para Producción
- **Usar**: Perfil `prod`
- **Ventaja**: Datos persistentes, rendimiento optimizado
- **Requisito**: MariaDB configurado
- **Comando**: `mvn spring-boot:run -Dspring-boot.run.profiles=prod`

### 🔄 Para CI/CD
- **Usar**: Perfil `dev`
- **Ventaja**: Sin dependencias externas, rápido
- **Configuración**: Variable de entorno `SPRING_PROFILES_ACTIVE=dev`

## 🚀 Migración Entre Perfiles

### De Desarrollo a Producción
1. ✅ Configurar MariaDB
2. ✅ Ejecutar `data-prod.sql`
3. ✅ Cambiar perfil: `-Dspring-boot.run.profiles=prod`
4. ✅ Verificar conexión y datos

### De Producción a Desarrollo
1. ✅ Cambiar perfil: `-Dspring-boot.run.profiles=dev`
2. ✅ Los datos H2 se crean automáticamente
3. ✅ No requiere configuración adicional

## 📝 Notas Importantes

- ⚠️ **H2 es solo para desarrollo**: Los datos se pierden al reiniciar
- ⚠️ **MariaDB requiere configuración**: Ver sección de configuración inicial
- ✅ **Cambio de perfil es instantáneo**: No requiere recompilar código
- ✅ **Perfiles son independientes**: Las configuraciones no se mezclan
- 🔒 **Producción es más segura**: Consola H2 deshabilitada, logs reducidos

## 🎉 Estado Actual del Proyecto

✅ **Sistema de perfiles completamente funcional**  
✅ **Error 500 en /admin/tracks solucionado**  
✅ **H2 configurado para desarrollo**  
✅ **MariaDB preparado para producción**  
✅ **Scripts auxiliares creados**  
✅ **Documentación completa**  

**¡La aplicación está lista para desarrollo y producción! 🚀**