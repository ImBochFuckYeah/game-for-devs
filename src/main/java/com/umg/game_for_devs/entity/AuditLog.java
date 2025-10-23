package com.umg.game_for_devs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Entidad que representa la bitácora de acciones administrativas
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El usuario es obligatorio")
    @Column(name = "username", nullable = false)
    private String username; // Usuario que realizó la acción
    
    @NotBlank(message = "La acción es obligatoria")
    @Column(name = "action", nullable = false)
    private String action; // Descripción de la acción realizada
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    @Column(name = "resource_type")
    private String resourceType; // Tipo de recurso afectado (Track, User, etc.)
    
    @Column(name = "resource_id")
    private Long resourceId; // ID del recurso afectado
    
    @Column(name = "resource_name")
    private String resourceName; // Nombre del recurso afectado
    
    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Detalles adicionales en formato JSON
    
    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress; // IP desde donde se realizó la acción
    
    @Column(name = "user_agent")
    private String userAgent; // Navegador/dispositivo utilizado
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.SUCCESS;
    
    @Column(name = "error_message")
    private String errorMessage; // En caso de error
    
    // Enums
    public enum ActionType {
        CREATE("Crear"),
        READ("Consultar"),
        UPDATE("Actualizar"),
        DELETE("Eliminar"),
        LOGIN("Iniciar Sesión"),
        LOGOUT("Cerrar Sesión"),
        EXPORT("Exportar"),
        IMPORT("Importar"),
        CONFIGURE("Configurar");
        
        private final String displayName;
        
        ActionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Status {
        SUCCESS("Exitoso"),
        FAILED("Fallido"),
        PARTIAL("Parcial");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(String username, String action, ActionType actionType) {
        this.username = username;
        this.action = action;
        this.actionType = actionType;
        this.timestamp = LocalDateTime.now();
        this.status = Status.SUCCESS;
    }
    
    public AuditLog(String username, String action, ActionType actionType, String resourceType, Long resourceId, String resourceName) {
        this(username, action, actionType);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", actionType=" + actionType +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }
}