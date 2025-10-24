package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.entity.Track;
import com.umg.game_for_devs.entity.GameSession;
import com.umg.game_for_devs.service.TrackService;
import com.umg.game_for_devs.repository.GameSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Controlador público para el juego Game For Devs
 * No requiere autenticación - acceso libre para todos los usuarios
 */
@Controller
public class GameController {

    @Autowired
    private TrackService trackService;

    @Autowired
    private GameSessionRepository gameSessionRepository;
    
    /**
     * Página principal del juego - Redirige al juego directamente
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/game";
    }
    
    /**
     * Página principal del juego
     * Carga una pista aleatoria y muestra la interfaz de juego
     */
    @GetMapping("/game")
    public String game(@RequestParam(required = false) Long trackId, Model model) {
        try {
            Track selectedTrack = null;
            
            // Si se especifica un trackId válido, intentar cargarlo
            if (trackId != null) {
                Optional<Track> track = trackService.getTrackById(trackId);
                if (track.isPresent() && track.get().getIsActive()) {
                    selectedTrack = track.get();
                }
            }
            
            // Si no se especificó trackId o no se encontró una pista válida, cargar una aleatoria
            if (selectedTrack == null) {
                Optional<Track> randomTrackOpt = trackService.getRandomTrack();
                if (randomTrackOpt.isPresent()) {
                    selectedTrack = randomTrackOpt.get();
                } else {
                    model.addAttribute("error", "No hay pistas disponibles en este momento");
                    model.addAttribute("pageTitle", "Game For Devs - Error");
                    return "game";
                }
            }
            
            model.addAttribute("track", selectedTrack);
            model.addAttribute("pageTitle", "Juego: " + selectedTrack.getName());
            return "game";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la pista del juego");
            model.addAttribute("pageTitle", "Game For Devs - Error");
            return "game";
        }
    }

    /**
     * API para obtener pista aleatoria
     */
    @GetMapping("/api/track/random")
    @ResponseBody
    public ResponseEntity<Track> getRandomTrack() {
        try {
            Optional<Track> trackOpt = trackService.getRandomTrack();
            if (trackOpt.isPresent()) {
                return ResponseEntity.ok(trackOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API para obtener pista por ID
     */
    @GetMapping("/api/track/{id}")
    @ResponseBody
    public ResponseEntity<Track> getTrack(@PathVariable Long id) {
        try {
            Optional<Track> track = trackService.getTrackById(id);
            return track.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API para guardar sesión de juego
     */
    @PostMapping("/api/game/session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveGameSession(
            @RequestBody GameSessionRequest request, 
            HttpServletRequest httpRequest) {
        
        try {
            // Buscar la pista
            Optional<Track> trackOpt = trackService.getTrackById(request.getTrackId());
            if (!trackOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Pista no encontrada"));
            }
            
            GameSession session = new GameSession();
            session.setSessionId("web_" + System.currentTimeMillis());
            session.setTrack(trackOpt.get());
            session.setStartTime(LocalDateTime.now().minusSeconds(request.getTimeSpent() != null ? request.getTimeSpent() : 0));
            
            if (request.isCompleted()) {
                session.setEndTime(LocalDateTime.now());
                session.setStatus(GameSession.GameStatus.SUCCESS);
            } else {
                session.setStatus(GameSession.GameStatus.IN_PROGRESS);
            }
            
            session.setMovesCount(request.getMoves());
            session.setIpAddress(getClientIp(httpRequest));
            session.setUserAgent(httpRequest.getHeader("User-Agent"));

            GameSession savedSession = gameSessionRepository.save(session);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", savedSession.getId(),
                "message", "Sesión guardada correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "Error al guardar sesión"));
        }
    }

    /**
     * API para obtener estadísticas básicas del juego
     */
    @GetMapping("/api/game/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGameStats() {
        try {
            long totalSessions = gameSessionRepository.count();
            long completedSessions = gameSessionRepository.countByStatus(GameSession.GameStatus.SUCCESS);
            double completionRate = totalSessions > 0 ? (double) completedSessions / totalSessions * 100 : 0;
            
            return ResponseEntity.ok(Map.of(
                "totalSessions", totalSessions,
                "completedSessions", completedSessions,
                "completionRate", Math.round(completionRate * 100.0) / 100.0
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Página de instrucciones del juego
     */
    @GetMapping("/instructions")
    public String instructions(Model model) {
        model.addAttribute("pageTitle", "Instrucciones - Game For Devs");
        return "game/instructions";
    }
    
    /**
     * Página de acceso denegado
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Acceso Denegado");
        model.addAttribute("message", "No tienes permisos para acceder a esta sección.");
        return "error/access-denied";
    }

    /**
     * Obtener IP del cliente considerando proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Clase interna para recibir datos de sesión de juego
     */
    public static class GameSessionRequest {
        private Long trackId;
        private boolean completed;
        private Integer score;
        private Integer timeSpent; // en segundos
        private Integer moves;
        private Integer hintsUsed;
        private Integer errorCount;

        // Getters y Setters
        public Long getTrackId() {
            return trackId;
        }

        public void setTrackId(Long trackId) {
            this.trackId = trackId;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Integer getTimeSpent() {
            return timeSpent;
        }

        public void setTimeSpent(Integer timeSpent) {
            this.timeSpent = timeSpent;
        }

        public Integer getMoves() {
            return moves;
        }

        public void setMoves(Integer moves) {
            this.moves = moves;
        }

        public Integer getHintsUsed() {
            return hintsUsed;
        }

        public void setHintsUsed(Integer hintsUsed) {
            this.hintsUsed = hintsUsed;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Integer errorCount) {
            this.errorCount = errorCount;
        }
    }
}