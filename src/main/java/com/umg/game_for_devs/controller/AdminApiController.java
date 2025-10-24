package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.dto.UserDto;
import com.umg.game_for_devs.entity.Track;
import com.umg.game_for_devs.entity.User;
import com.umg.game_for_devs.entity.AuditLog;
import com.umg.game_for_devs.service.UserService;
import com.umg.game_for_devs.service.TrackService;
import com.umg.game_for_devs.service.StatisticsService;
import com.umg.game_for_devs.repository.AuditLogRepository;
import com.umg.game_for_devs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controlador REST para las APIs administrativas
 * Proporciona endpoints para gestión de usuarios, pistas, auditoría y estadísticas
 */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private TrackService trackService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;

    // ==================== USUARIOS ====================

    /**
     * Obtener lista paginada de usuarios con filtros
     */
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy, sortDir, search, role));
    }

    /**
     * Buscar usuarios por criterios
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String searchTerm) {
        return ResponseEntity.ok(userService.searchUsers(searchTerm));
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crear nuevo usuario
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto, Authentication auth) {
        try {
            // Validar campos requeridos usando el DTO
            if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario es obligatorio"));
            }
            if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
            }
            if (userDto.getPassword() == null || userDto.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
            }
            if (userDto.getRole() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El rol es obligatorio"));
            }
            
            // Verificar si el usuario ya existe
            if (userRepository.existsByUsername(userDto.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
            }
            if (userRepository.existsByEmail(userDto.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
            }
            
            // Crear usuario desde DTO
            User user = new User();
            user.setUsername(userDto.getUsername().trim());
            user.setEmail(userDto.getEmail().trim());
            user.setPassword(userDto.getPassword()); // Se encriptará en el servicio
            user.setRole(userDto.getRole());
            
            // Procesar nombres
            String fullName = "";
            if (userDto.getFirstName() != null && !userDto.getFirstName().trim().isEmpty()) {
                fullName += userDto.getFirstName().trim();
            }
            if (userDto.getLastName() != null && !userDto.getLastName().trim().isEmpty()) {
                if (!fullName.isEmpty()) fullName += " ";
                fullName += userDto.getLastName().trim();
            }
            user.setFullName(fullName.isEmpty() ? userDto.getUsername() : fullName);
            
            // Estado activo
            user.setIsActive(userDto.getActive() != null ? userDto.getActive() : true);
            
            User createdUser = userService.createUser(user, auth.getName());
            return ResponseEntity.ok(createdUser);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al crear usuario: " + e.getMessage()));
        }
    }

    /**
     * Actualizar usuario existente
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto, Authentication auth) {
        try {
            // Validar campos requeridos usando el DTO
            if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario es obligatorio"));
            }
            if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
            }
            if (userDto.getRole() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El rol es obligatorio"));
            }
            
            // Verificar si el usuario existe
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (!existingUserOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar unicidad (excluyendo el usuario actual)
            if (userRepository.existsByUsernameAndIdNot(userDto.getUsername(), id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
            }
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
            }
            
            User user = existingUserOpt.get();
            user.setUsername(userDto.getUsername().trim());
            user.setEmail(userDto.getEmail().trim());
            
            // Actualizar contraseña solo si se proporciona
            if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
                if (userDto.getPassword().length() < 6) {
                    return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
                }
                user.setPassword(userDto.getPassword()); // Se encriptará en el servicio
            }
            
            // Actualizar rol
            user.setRole(userDto.getRole());
            
            // Procesar nombres
            String fullName = "";
            if (userDto.getFirstName() != null && !userDto.getFirstName().trim().isEmpty()) {
                fullName += userDto.getFirstName().trim();
            }
            if (userDto.getLastName() != null && !userDto.getLastName().trim().isEmpty()) {
                if (!fullName.isEmpty()) fullName += " ";
                fullName += userDto.getLastName().trim();
            }
            user.setFullName(fullName.isEmpty() ? userDto.getUsername() : fullName);
            
            // Estado activo
            user.setIsActive(userDto.getActive() != null ? userDto.getActive() : true);
            
            User updatedUser = userService.updateUser(id, user, auth.getName());
            return ResponseEntity.ok(updatedUser);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al actualizar usuario: " + e.getMessage()));
        }
    }

    /**
     * Eliminar usuario
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        userService.deleteUser(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Cambiar contraseña de usuario
     */
    @PatchMapping("/users/{id}/password")
    public ResponseEntity<Void> changeUserPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication auth) {
        String newPassword = payload.get("password");
        userService.changePassword(id, newPassword, auth.getName());
        return ResponseEntity.ok().build();
    }

    // ==================== PISTAS ====================

    /**
     * Obtener lista paginada de pistas
     */
    @GetMapping("/tracks")
    public ResponseEntity<List<Track>> getTracks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<Track> trackPage = trackService.getAllTracks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(trackPage.getContent());
    }

    /**
     * Buscar pistas por criterios
     */
    @GetMapping("/tracks/search")
    public ResponseEntity<List<Track>> searchTracks(
            @RequestParam(required = false) String searchTerm) {
        return ResponseEntity.ok(trackService.searchTracks(searchTerm));
    }

    /**
     * Obtener pista por ID
     */
    @GetMapping("/tracks/{id}")
    public ResponseEntity<Track> getTrack(@PathVariable Long id) {
        return trackService.getTrackById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crear nueva pista
     */
    @PostMapping("/tracks")
    public ResponseEntity<?> createTrack(@Valid @RequestBody Track track, Authentication auth) {
        try {
            Track createdTrack = trackService.createTrack(track, auth.getName());
            return ResponseEntity.ok(createdTrack);
        } catch (Exception e) {
            e.printStackTrace(); // Para debug
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor", "details", e.getMessage()));
        }
    }

    /**
     * Actualizar pista existente
     */
    @PutMapping("/tracks/{id}")
    public ResponseEntity<Track> updateTrack(@PathVariable Long id, @Valid @RequestBody Track track, Authentication auth) {
        Track updatedTrack = trackService.updateTrack(id, track, auth.getName());
        return ResponseEntity.ok(updatedTrack);
    }

    /**
     * Eliminar pista
     */
    @DeleteMapping("/tracks/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id, Authentication auth) {
        trackService.deleteTrack(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Importar pistas desde archivo
     */
    @PostMapping("/tracks/import")
    public ResponseEntity<?> importTracks(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, Authentication auth) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
            }
            
            // Por ahora retornamos un mensaje de éxito simple
            // En el futuro se puede implementar la lógica de importación
            return ResponseEntity.ok(Map.of(
                "message", "Funcionalidad de importación en desarrollo",
                "filename", file.getOriginalFilename()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al importar: " + e.getMessage()));
        }
    }

    /**
     * Exportar pistas a archivo
     */
    @GetMapping("/tracks/export")
    public ResponseEntity<?> exportTracks(@RequestParam(defaultValue = "false") boolean activeOnly) {
        try {
            // Por ahora retornamos un mensaje de éxito simple
            // En el futuro se puede implementar la lógica de exportación real
            List<Track> tracks = activeOnly ? 
                trackService.getAllActiveTracks() : 
                trackService.getAllTracks(0, Integer.MAX_VALUE, "id", "asc").getContent();
                
            return ResponseEntity.ok(Map.of(
                "message", "Funcionalidad de exportación en desarrollo", 
                "totalTracks", tracks.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al exportar: " + e.getMessage()));
        }
    }

    // ==================== AUDITORÍA ====================

    /**
     * Obtener logs de auditoría paginados
     */
    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditLogRepository.findAll(pageRequest));
    }

    /**
     * Buscar logs de auditoría por criterios
     */
    @GetMapping("/audit/search")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @RequestParam(required = false) AuditLog.ActionType action,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = (toDate != null) ? toDate.atTime(23, 59, 59) : null;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());
        
        return ResponseEntity.ok(auditLogRepository.findByFilters(
                username, action, entity, null, fromDateTime, toDateTime, pageRequest));
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtener estadísticas del dashboard
     */
    @GetMapping("/statistics/dashboard")
    public ResponseEntity<StatisticsService.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(statisticsService.getDashboardStats());
    }

    /**
     * Obtener estadísticas de éxito por pista
     */
    @GetMapping("/statistics/tracks")
    public ResponseEntity<List<StatisticsService.TrackSuccessStats>> getTrackSuccessStats() {
        return ResponseEntity.ok(statisticsService.getTrackSuccessStats());
    }

    /**
     * Obtener estadísticas por dispositivo
     */
    @GetMapping("/statistics/devices")
    public ResponseEntity<List<StatisticsService.DeviceStats>> getDeviceStats() {
        return ResponseEntity.ok(statisticsService.getDeviceStats());
    }

    // ==================== MANEJO DE ERRORES ====================

    /**
     * Manejo de errores de validación
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Manejo de errores generales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralError(Exception e) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error interno del servidor"));
    }
}