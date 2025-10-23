-- Datos de producción para MariaDB
-- Este script debe ejecutarse manualmente en producción

-- Insertar usuario administrador por defecto
INSERT INTO users (username, password, email, full_name, role, is_active, created_at, updated_at, created_by) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@gamefordevs.com', 'Administrador del Sistema', 'ADMIN', true, NOW(), NOW(), 'system')
ON DUPLICATE KEY UPDATE password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.';

-- Pistas de producción
INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'Introducción - Primer Paso',
    '[
        [0, 0, 0, 0, 0],
        [1, 1, 1, 1, 0],
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0]
    ]',
    0, 1, 'EAST', NOW(), NOW(), 'admin', true, 1,
    'Tu primera aventura en el mundo del código. Aprende los movimientos básicos.'
)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'Giros y Decisiones',
    '[
        [0, 0, 0, 0, 0],
        [0, 1, 1, 1, 0],
        [0, 1, 0, 0, 0],
        [1, 1, 0, 0, 0]
    ]',
    0, 3, 'EAST', NOW(), NOW(), 'admin', true, 2,
    'Aprende a tomar decisiones y cambiar de dirección en tu código.'
)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'El Laberinto del Pensamiento',
    '[
        [0, 1, 1, 1, 0],
        [0, 1, 0, 1, 0],
        [0, 1, 0, 1, 0],
        [1, 1, 0, 1, 0]
    ]',
    0, 3, 'NORTH', NOW(), NOW(), 'admin', true, 3,
    'Desafía tu lógica navegando por caminos complejos.'
)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'El Espiral del Conocimiento',
    '[
        [1, 1, 1, 1, 0],
        [0, 0, 0, 1, 0],
        [0, 1, 1, 1, 0],
        [0, 1, 0, 0, 0]
    ]',
    0, 0, 'EAST', NOW(), NOW(), 'admin', true, 4,
    'Un desafío avanzado que pondrá a prueba tu planificación.'
)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'La Matriz del Maestro',
    '[
        [1, 1, 0, 1, 1],
        [1, 0, 0, 0, 1],
        [0, 0, 1, 0, 0],
        [1, 0, 1, 1, 1]
    ]',
    0, 0, 'EAST', NOW(), NOW(), 'admin', true, 5,
    'Solo los verdaderos maestros del código pueden completar este desafío.'
)
ON DUPLICATE KEY UPDATE id=id;

-- Log inicial de producción
INSERT INTO audit_logs (username, action, action_type, resource_type, resource_id, resource_name, details, timestamp, ip_address, user_agent, status)
VALUES 
('admin', 'Inicialización del sistema de producción', 'CONFIGURE', 'System', NULL, 'Game For Devs', '{"environment": "production", "initial_tracks": 5, "version": "1.0"}', NOW(), '0.0.0.0', 'Production System', 'SUCCESS');

-- Mensaje de confirmación
SELECT 'Sistema inicializado correctamente para producción' as mensaje;