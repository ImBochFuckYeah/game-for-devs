package com.umg.game_for_devs.controller.api;

import com.umg.game_for_devs.entity.GameSession;
import com.umg.game_for_devs.entity.Track;
import com.umg.game_for_devs.repository.GameSessionRepository;
import com.umg.game_for_devs.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * API REST público para el juego
 * No requiere autenticación
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameApiController {
    
    @Autowired
    private TrackRepository trackRepository;
    
    @Autowired
    private GameSessionRepository gameSessionRepository;
    
    /**
     * Endpoint de debug para verificar pistas disponibles
     */
    @GetMapping("/tracks/debug")
    public ResponseEntity<Map<String, Object>> getTracksDebug() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            long totalTracks = trackRepository.count();
            long activeTracks = trackRepository.countByIsActiveTrue();
            List<Track> allTracks = trackRepository.findAll();
            
            debug.put("totalTracks", totalTracks);
            debug.put("activeTracks", activeTracks);
            debug.put("tracks", allTracks.stream().map(track -> {
                Map<String, Object> trackInfo = new HashMap<>();
                trackInfo.put("id", track.getId());
                trackInfo.put("name", track.getName());
                trackInfo.put("isActive", track.getIsActive());
                return trackInfo;
            }).toList());
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            return ResponseEntity.status(500).body(debug);
        }
    }
    
    /**
     * Obtiene una pista aleatoria para jugar
     */
    @GetMapping("/track/random")
    public ResponseEntity<Map<String, Object>> getRandomTrack(@RequestParam(required = false) Long excludeId) {
        try {
            Optional<Track> trackOpt;
            
            // Si se especifica una pista a excluir, intentar obtener una diferente
            if (excludeId != null) {
                trackOpt = trackRepository.findRandomActiveTrackExcluding(excludeId);
                // Si no hay otra pista disponible, obtener cualquiera
                if (trackOpt.isEmpty()) {
                    trackOpt = trackRepository.findRandomActiveTrack();
                }
            } else {
                trackOpt = trackRepository.findRandomActiveTrack();
            }
            
            if (trackOpt.isPresent()) {
                Track track = trackOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", track.getId());
                response.put("name", track.getName());
                response.put("gridConfig", track.getGridConfig());
                response.put("startX", track.getStartX());
                response.put("startY", track.getStartY());
                response.put("startDirection", track.getStartDirection());
                response.put("difficultyLevel", track.getDifficultyLevel());
                response.put("description", track.getDescription());
                
                // Información adicional sobre si es una pista diferente
                if (excludeId != null) {
                    response.put("isDifferent", !track.getId().equals(excludeId));
                    response.put("excludedId", excludeId);
                }
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No hay pistas disponibles");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Inicia una nueva sesión de juego
     */
    @PostMapping("/session/start")
    public ResponseEntity<Map<String, Object>> startGameSession(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            Long trackId = Long.valueOf(request.get("trackId").toString());
            String sessionId = UUID.randomUUID().toString();
            
            Optional<Track> trackOpt = trackRepository.findById(trackId);
            if (!trackOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Pista no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Track track = trackOpt.get();
            GameSession session = new GameSession(sessionId, track);
            
            // Información adicional de la sesión
            session.setIpAddress(getClientIpAddress(httpRequest));
            session.setUserAgent(httpRequest.getHeader("User-Agent"));
            session.setDeviceType(detectDeviceType(httpRequest.getHeader("User-Agent")));
            
            // Calcular total de celdas requeridas
            session.setTotalCellsRequired(calculateTotalCells(track.getGridConfig()));
            
            gameSessionRepository.save(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("trackId", trackId);
            response.put("message", "Sesión iniciada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al iniciar sesión: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Actualiza una sesión de juego con el progreso
     */
    @PutMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateGameSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            Optional<GameSession> sessionOpt = gameSessionRepository.findBySessionId(sessionId);
            if (!sessionOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Sesión no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            GameSession session = sessionOpt.get();
            
            // Actualizar información de la sesión
            if (request.containsKey("movesCount")) {
                session.setMovesCount(Integer.valueOf(request.get("movesCount").toString()));
            }
            
            if (request.containsKey("movesSequence")) {
                session.setMovesSequence(request.get("movesSequence").toString());
            }
            
            if (request.containsKey("cellsVisited")) {
                session.setCellsVisited(Integer.valueOf(request.get("cellsVisited").toString()));
            }
            
            if (request.containsKey("attemptsCount")) {
                session.setAttemptsCount(Integer.valueOf(request.get("attemptsCount").toString()));
            }
            
            if (request.containsKey("status")) {
                String status = request.get("status").toString();
                session.setStatus(GameSession.GameStatus.valueOf(status));
                
                if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
                    session.setEndTime(LocalDateTime.now());
                    
                    if (request.containsKey("executionTimeMs")) {
                        session.setExecutionTimeMs(Long.valueOf(request.get("executionTimeMs").toString()));
                    }
                    
                    if ("FAILED".equals(status) && request.containsKey("errorMessage")) {
                        session.setErrorMessage(request.get("errorMessage").toString());
                        
                        if (request.containsKey("errorPositionX")) {
                            session.setErrorPositionX(Integer.valueOf(request.get("errorPositionX").toString()));
                        }
                        
                        if (request.containsKey("errorPositionY")) {
                            session.setErrorPositionY(Integer.valueOf(request.get("errorPositionY").toString()));
                        }
                    }
                }
            }
            
            gameSessionRepository.save(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sesión actualizada exitosamente");
            response.put("sessionId", sessionId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al actualizar sesión: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Obtiene estadísticas básicas para mostrar al usuario
     */
    @GetMapping("/stats/basic")
    public ResponseEntity<Map<String, Object>> getBasicStats() {
        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime now = LocalDateTime.now();
            
            long totalSessions = gameSessionRepository.countByStartTimeBetween(weekAgo, now);
            long successfulSessions = gameSessionRepository.countByStatus(GameSession.GameStatus.SUCCESS);
            long totalTracks = trackRepository.countByIsActiveTrue();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalSessionsThisWeek", totalSessions);
            response.put("successfulSessions", successfulSessions);
            response.put("availableTracks", totalTracks);
            response.put("successRate", totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Métodos auxiliares
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "MOBILE";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "TABLET";
        } else {
            return "DESKTOP";
        }
    }
    
    private Integer calculateTotalCells(String gridConfig) {
        // Esta función debería parsear el JSON del grid y contar las celdas con valor 1
        // Por simplicidad, retornamos un valor por defecto
        // TODO: Implementar parsing real del JSON
        return 6; // Valor por defecto
    }
}