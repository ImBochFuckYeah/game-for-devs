package com.umg.game_for_devs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Entidad que representa una sesión de juego para estadísticas
 */
@Entity
@Table(name = "game_sessions")
public class GameSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId; // Identificador único de la sesión del navegador
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track; // Pista que se jugó
    
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // Momento en que inició el juego
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // Momento en que terminó el juego
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status = GameStatus.IN_PROGRESS;
    
    @Column(name = "moves_count")
    private Integer movesCount = 0; // Cantidad de movimientos programados
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs; // Tiempo de ejecución en millisegundos
    
    @Column(name = "error_position_x")
    private Integer errorPositionX; // Posición X donde ocurrió el error (si aplica)
    
    @Column(name = "error_position_y")
    private Integer errorPositionY; // Posición Y donde ocurrió el error (si aplica)
    
    @Column(name = "error_message")
    private String errorMessage; // Mensaje de error específico
    
    @Column(name = "ip_address")
    private String ipAddress; // IP del usuario
    
    @Column(name = "user_agent")
    private String userAgent; // Navegador/dispositivo
    
    @Column(name = "screen_resolution")
    private String screenResolution; // Resolución de pantalla
    
    @Column(name = "device_type")
    private String deviceType; // DESKTOP, MOBILE, TABLET
    
    @Lob
    @Column(name = "moves_sequence", columnDefinition = "TEXT")
    private String movesSequence; // Secuencia de movimientos en formato JSON
    
    @Column(name = "attempts_count")
    private Integer attemptsCount = 1; // Cantidad de intentos en esta sesión
    
    @Column(name = "cells_visited")
    private Integer cellsVisited = 0; // Cantidad de celdas verdes visitadas
    
    @Column(name = "total_cells_required")
    private Integer totalCellsRequired; // Total de celdas verdes requeridas para completar
    
    // Enums
    public enum GameStatus {
        IN_PROGRESS("En Progreso"),
        SUCCESS("Completado Exitosamente"),
        FAILED("Fallido"),
        ABANDONED("Abandonado"),
        ERROR("Error Técnico");
        
        private final String displayName;
        
        GameStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public GameSession() {}
    
    public GameSession(String sessionId, Track track) {
        this.sessionId = sessionId;
        this.track = track;
        this.startTime = LocalDateTime.now();
        this.status = GameStatus.IN_PROGRESS;
        this.movesCount = 0;
        this.attemptsCount = 1;
        this.cellsVisited = 0;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }
    
    // Helper methods
    public Long getDurationMs() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return null;
    }
    
    public Double getCompletionPercentage() {
        if (totalCellsRequired != null && totalCellsRequired > 0) {
            return (cellsVisited.doubleValue() / totalCellsRequired.doubleValue()) * 100.0;
        }
        return 0.0;
    }
    
    public void markAsCompleted() {
        this.status = GameStatus.SUCCESS;
        this.endTime = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = GameStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Track getTrack() {
        return track;
    }
    
    public void setTrack(Track track) {
        this.track = track;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public void setStatus(GameStatus status) {
        this.status = status;
    }
    
    public Integer getMovesCount() {
        return movesCount;
    }
    
    public void setMovesCount(Integer movesCount) {
        this.movesCount = movesCount;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public Integer getErrorPositionX() {
        return errorPositionX;
    }
    
    public void setErrorPositionX(Integer errorPositionX) {
        this.errorPositionX = errorPositionX;
    }
    
    public Integer getErrorPositionY() {
        return errorPositionY;
    }
    
    public void setErrorPositionY(Integer errorPositionY) {
        this.errorPositionY = errorPositionY;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getScreenResolution() {
        return screenResolution;
    }
    
    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getMovesSequence() {
        return movesSequence;
    }
    
    public void setMovesSequence(String movesSequence) {
        this.movesSequence = movesSequence;
    }
    
    public Integer getAttemptsCount() {
        return attemptsCount;
    }
    
    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }
    
    public Integer getCellsVisited() {
        return cellsVisited;
    }
    
    public void setCellsVisited(Integer cellsVisited) {
        this.cellsVisited = cellsVisited;
    }
    
    public Integer getTotalCellsRequired() {
        return totalCellsRequired;
    }
    
    public void setTotalCellsRequired(Integer totalCellsRequired) {
        this.totalCellsRequired = totalCellsRequired;
    }
    
    @Override
    public String toString() {
        return "GameSession{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", track=" + (track != null ? track.getName() : "null") +
                ", status=" + status +
                ", movesCount=" + movesCount +
                ", attemptsCount=" + attemptsCount +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}