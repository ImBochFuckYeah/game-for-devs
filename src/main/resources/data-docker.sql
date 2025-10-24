-- Script de inicialización compatible con MariaDB para Game For Devs
-- Este script crea algunos datos de prueba para el desarrollo

-- Insertar usuario administrador por defecto (con INSERT IGNORE para evitar duplicados)
-- Contraseña: admin123 (hash BCrypt)
INSERT IGNORE INTO users (username, password, email, full_name, role, is_active, created_at, updated_at, created_by) 
VALUES ('admin', '$2a$10$2z6GiyUjiwzXml5FvN1mmeKkkhCse2LHC9O0vaAgDDK2yFUv6V4qe', 'admin@gamefordevs.com', 'Administrador del Sistema', 'ADMIN', true, NOW(), NOW(), 'system');

-- Insertar pista de ejemplo 1: Camino en L (con INSERT IGNORE para evitar duplicados)
INSERT IGNORE INTO tracks (id, name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    1,
    'Camino en L',
    '[
        [0, 0, 0, 0, 0],
        [0, 1, 1, 1, 0],
        [0, 1, 0, 0, 0],
        [1, 1, 0, 0, 0]
    ]',
    0, 3, 'EAST', NOW(), NOW(), 'admin', true, 1,
    'Una pista básica en forma de L para principiantes'
);

-- Insertar pista de ejemplo 2: Camino recto (con INSERT IGNORE para evitar duplicados)
INSERT IGNORE INTO tracks (id, name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    2,
    'Camino Recto',
    '[
        [0, 0, 0, 0, 0],
        [1, 1, 1, 1, 0],
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0]
    ]',
    0, 1, 'EAST', NOW(), NOW(), 'admin', true, 1,
    'Un camino recto simple para comenzar'
);

-- Insertar pista de ejemplo 3: Laberinto básico (con INSERT IGNORE para evitar duplicados)
INSERT IGNORE INTO tracks (id, name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    3,
    'Laberinto Básico',
    '[
        [0, 1, 1, 1, 0],
        [0, 1, 0, 1, 0],
        [0, 1, 0, 1, 0],
        [1, 1, 0, 1, 0]
    ]',
    0, 3, 'NORTH', NOW(), NOW(), 'admin', true, 3,
    'Un laberinto más desafiante que requiere planificación'
);

-- Insertar algunos logs de auditoría de ejemplo (con INSERT IGNORE para evitar duplicados)
INSERT IGNORE INTO audit_logs (id, username, action, action_type, resource_type, resource_id, resource_name, details, timestamp, ip_address, user_agent, status)
VALUES 
(1, 'admin', 'Creación de pista inicial: Camino en L', 'CREATE', 'Track', 1, 'Camino en L', '{"difficulty": 1, "grid_size": "5x4"}', NOW(), '127.0.0.1', 'Mozilla/5.0 (System)', 'SUCCESS'),
(2, 'admin', 'Creación de pista inicial: Camino Recto', 'CREATE', 'Track', 2, 'Camino Recto', '{"difficulty": 1, "grid_size": "5x4"}', NOW(), '127.0.0.1', 'Mozilla/5.0 (System)', 'SUCCESS'),
(3, 'admin', 'Creación de pista inicial: Laberinto Básico', 'CREATE', 'Track', 3, 'Laberinto Básico', '{"difficulty": 3, "grid_size": "5x4"}', NOW(), '127.0.0.1', 'Mozilla/5.0 (System)', 'SUCCESS'),
(4, 'admin', 'Inicialización del sistema completada', 'CONFIGURE', 'System', NULL, 'Game For Devs', '{"initial_tracks": 3, "initial_admin": 1}', NOW(), '127.0.0.1', 'Mozilla/5.0 (System)', 'SUCCESS');

-- Crear algunas sesiones de juego de ejemplo para estadísticas (usando DATE_ADD compatible con MariaDB)
INSERT IGNORE INTO game_sessions (session_id, track_id, start_time, end_time, status, moves_count, execution_time_ms, ip_address, user_agent, device_type, moves_sequence, attempts_count, cells_visited, total_cells_required)
VALUES 
('demo-session-1', 1, DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL -118 MINUTE), 'SUCCESS', 6, 3500, '192.168.1.100', 'Mozilla/5.0 (Desktop)', 'DESKTOP', '[{"type":"RIGHT"},{"type":"FORWARD"},{"type":"FORWARD"},{"type":"LEFT"},{"type":"FORWARD"},{"type":"FORWARD"}]', 1, 6, 6),
('demo-session-2', 2, DATE_ADD(NOW(), INTERVAL -1 HOUR), DATE_ADD(NOW(), INTERVAL -58 MINUTE), 'SUCCESS', 3, 2000, '192.168.1.101', 'Mozilla/5.0 (Mobile)', 'MOBILE', '[{"type":"FORWARD"},{"type":"FORWARD"},{"type":"FORWARD"}]', 1, 4, 4),
('demo-session-3', 1, DATE_ADD(NOW(), INTERVAL -30 MINUTE), DATE_ADD(NOW(), INTERVAL -28 MINUTE), 'FAILED', 4, NULL, '192.168.1.102', 'Mozilla/5.0 (Tablet)', 'TABLET', '[{"type":"FORWARD"},{"type":"FORWARD"},{"type":"FORWARD"},{"type":"FORWARD"}]', 1, 2, 6),
('demo-session-4', 3, DATE_ADD(NOW(), INTERVAL -15 MINUTE), DATE_ADD(NOW(), INTERVAL -10 MINUTE), 'SUCCESS', 12, 8500, '192.168.1.103', 'Mozilla/5.0 (Desktop)', 'DESKTOP', '[{"type":"UP"},{"type":"UP"},{"type":"RIGHT"},{"type":"DOWN"},{"type":"RIGHT"},{"type":"UP"},{"type":"UP"},{"type":"RIGHT"},{"type":"DOWN"},{"type":"DOWN"},{"type":"DOWN"},{"type":"LEFT"}]', 2, 12, 12);

-- Mensaje de confirmación
SELECT 'Datos iniciales insertados correctamente para Game For Devs' as mensaje;