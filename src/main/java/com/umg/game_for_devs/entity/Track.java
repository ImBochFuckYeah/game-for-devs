package com.umg.game_for_devs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entidad que representa una pista del juego
 */
@Entity
@Table(name = "tracks")
public class Track {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre de la pista es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @NotNull(message = "La configuración de la pista es obligatoria")
    @Lob
    @Column(name = "grid_config", nullable = false, columnDefinition = "TEXT")
    private String gridConfig; // JSON que representa el grid 5x4
    
    @NotNull
    @Column(name = "start_x", nullable = false)
    private Integer startX; // Posición inicial X del robot
    
    @NotNull
    @Column(name = "start_y", nullable = false)
    private Integer startY; // Posición inicial Y del robot
    
    @NotNull
    @Column(name = "start_direction", nullable = false)
    private String startDirection; // NORTH, SOUTH, EAST, WEST
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy; // Username del administrador que la creó
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Para soft delete
    
    @Column(name = "difficulty_level")
    private Integer difficultyLevel = 1; // 1-5 para clasificar dificultad
    
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Descripción opcional de la pista
    
    // Constructors
    public Track() {}
    
    public Track(String name, String gridConfig, Integer startX, Integer startY, String startDirection, String createdBy) {
        this.name = name;
        this.gridConfig = gridConfig;
        this.startX = startX;
        this.startY = startY;
        this.startDirection = startDirection;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGridConfig() {
        return gridConfig;
    }
    
    public void setGridConfig(String gridConfig) {
        this.gridConfig = gridConfig;
    }
    
    public Integer getStartX() {
        return startX;
    }
    
    public void setStartX(Integer startX) {
        this.startX = startX;
    }
    
    public Integer getStartY() {
        return startY;
    }
    
    public void setStartY(Integer startY) {
        this.startY = startY;
    }
    
    public String getStartDirection() {
        return startDirection;
    }
    
    public void setStartDirection(String startDirection) {
        this.startDirection = startDirection;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startX=" + startX +
                ", startY=" + startY +
                ", startDirection='" + startDirection + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", isActive=" + isActive +
                ", difficultyLevel=" + difficultyLevel +
                '}';
    }
}