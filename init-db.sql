-- Script de inicialización para la base de datos en Docker
-- Este script se ejecuta automáticamente cuando se crea el contenedor de MariaDB

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS game_for_devs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE game_for_devs;

-- Crear el usuario spring si no existe (con la contraseña correcta)
CREATE USER IF NOT EXISTS 'spring'@'%' IDENTIFIED BY 'Sefgag$$2024';

-- Otorgar todos los privilegios al usuario spring sobre la base de datos game_for_devs
GRANT ALL PRIVILEGES ON game_for_devs.* TO 'spring'@'%';

-- Aplicar los cambios
FLUSH PRIVILEGES;

-- Mensaje de confirmación (solo visible en logs del contenedor)
SELECT 'Base de datos game_for_devs inicializada correctamente - Usuario spring creado' as message;