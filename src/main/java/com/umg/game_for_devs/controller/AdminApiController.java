package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.dto.UserDto;
import com.umg.game_for_devs.entity.Track;
import com.umg.game_for_devs.entity.User;
import com.umg.game_for_devs.entity.AuditLog;
import com.umg.game_for_devs.service.UserService;
import com.umg.game_for_devs.service.TrackService;
import com.umg.game_for_devs.service.StatisticsService;
import com.umg.game_for_devs.repository.AuditLogRepository;
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

    // ==================== USUARIOS ====================

    /**
     * Obtener lista paginada de usuarios
     */
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy, sortDir));
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
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto, Authentication auth) {
        // Convertir DTO a Entity
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setRole(userDto.getRole());
        // Combinar firstName y lastName en fullName
        String fullName = "";
        if (userDto.getFirstName() != null) fullName += userDto.getFirstName();
        if (userDto.getLastName() != null) {
            if (!fullName.isEmpty()) fullName += " ";
            fullName += userDto.getLastName();
        }
        user.setFullName(fullName.isEmpty() ? userDto.getUsername() : fullName);
        user.setIsActive(userDto.getActive() != null ? userDto.getActive() : true);
        
        User createdUser = userService.createUser(user, auth.getName());
        return ResponseEntity.ok(createdUser);
    }

    /**
     * Actualizar usuario existente
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto, Authentication auth) {
        // Convertir DTO a Entity
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setRole(userDto.getRole());
        // Combinar firstName y lastName en fullName
        String fullName = "";
        if (userDto.getFirstName() != null) fullName += userDto.getFirstName();
        if (userDto.getLastName() != null) {
            if (!fullName.isEmpty()) fullName += " ";
            fullName += userDto.getLastName();
        }
        user.setFullName(fullName.isEmpty() ? userDto.getUsername() : fullName);
        user.setIsActive(userDto.getActive() != null ? userDto.getActive() : true);
        
        User updatedUser = userService.updateUser(id, user, auth.getName());
        return ResponseEntity.ok(updatedUser);
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
    public ResponseEntity<Page<Track>> getTracks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(trackService.getAllTracks(page, size, sortBy, sortDir));
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
    public ResponseEntity<Track> createTrack(@Valid @RequestBody Track track, Authentication auth) {
        Track createdTrack = trackService.createTrack(track, auth.getName());
        return ResponseEntity.ok(createdTrack);
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