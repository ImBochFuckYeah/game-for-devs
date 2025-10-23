-- Datos de desarrollo para H2
-- Script optimizado para desarrollo y testing

-- Insertar usuario administrador por defecto
INSERT INTO users (username, password, email, full_name, role, is_active, created_at, updated_at, created_by) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@gamefordevs.com', 'Administrador del Sistema', 'ADMIN', true, NOW(), NOW(), 'system');

-- Insertar usuario de prueba adicional
INSERT INTO users (username, password, email, full_name, role, is_active, created_at, updated_at, created_by) 
VALUES ('testuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'test@gamefordevs.com', 'Usuario de Prueba', 'ADMIN', true, NOW(), NOW(), 'admin');

-- Pistas de desarrollo con configuraciones simples
INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'Dev - Camino Simple',
    '[
        [0, 0, 0, 0, 0],
        [1, 1, 1, 0, 0],
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0]
    ]',
    0, 1, 'EAST', NOW(), NOW(), 'admin', true, 1,
    'Pista simple para desarrollo y testing'
);

INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'Dev - Camino en L',
    '[
        [0, 0, 0, 0, 0],
        [0, 1, 1, 1, 0],
        [0, 1, 0, 0, 0],
        [1, 1, 0, 0, 0]
    ]',
    0, 3, 'EAST', NOW(), NOW(), 'admin', true, 2,
    'Pista en L para testing de giros'
);

INSERT INTO tracks (name, grid_config, start_x, start_y, start_direction, created_at, updated_at, created_by, is_active, difficulty_level, description)
VALUES (
    'Dev - Test Complex',
    '[
        [0, 1, 1, 1, 0],
        [0, 1, 0, 1, 0],
        [0, 1, 0, 1, 0],
        [1, 1, 0, 1, 0]
    ]',
    0, 3, 'NORTH', NOW(), NOW(), 'admin', true, 3,
    'Pista compleja para testing avanzado'
);

-- Logs de auditoría para desarrollo
INSERT INTO audit_logs (username, action, action_type, resource_type, resource_id, resource_name, details, timestamp, ip_address, user_agent, status)
VALUES 
('admin', 'Inicialización de datos de desarrollo', 'CREATE', 'System', NULL, 'Dev Environment', '{"environment": "development", "tracks": 3, "users": 2}', NOW(), '127.0.0.1', 'Dev Environment', 'SUCCESS'),
('admin', 'Creación de pista de desarrollo', 'CREATE', 'Track', 1, 'Dev - Camino Simple', '{"difficulty": 1, "type": "development"}', NOW(), '127.0.0.1', 'Dev Environment', 'SUCCESS');

-- Sesiones de juego de prueba (usando DATEADD para H2)
INSERT INTO game_sessions (session_id, track_id, start_time, end_time, status, moves_count, execution_time_ms, ip_address, user_agent, device_type, moves_sequence, attempts_count, cells_visited, total_cells_required)
VALUES 
('dev-session-1', 1, DATEADD('HOUR', -1, NOW()), DATEADD('MINUTE', -58, NOW()), 'SUCCESS', 3, 1500, '127.0.0.1', 'Dev Browser', 'DESKTOP', '[{"type":"FORWARD"},{"type":"FORWARD"},{"type":"FORWARD"}]', 1, 3, 3),
('dev-session-2', 2, DATEADD('MINUTE', -30, NOW()), DATEADD('MINUTE', -28, NOW()), 'SUCCESS', 6, 2500, '127.0.0.1', 'Dev Browser', 'DESKTOP', '[{"type":"FORWARD"},{"type":"RIGHT"},{"type":"FORWARD"},{"type":"LEFT"},{"type":"FORWARD"}]', 1, 5, 6);

-- Mensaje de confirmación
SELECT 'Datos de desarrollo cargados correctamente en H2' as mensaje;