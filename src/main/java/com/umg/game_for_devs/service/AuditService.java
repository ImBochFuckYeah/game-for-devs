package com.umg.game_for_devs.service;

import com.umg.game_for_devs.entity.AuditLog;
import com.umg.game_for_devs.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Servicio para el registro automático de auditoría
 */
@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Registra una acción de auditoría
     */
    public void logAction(String action, AuditLog.ActionType actionType) {
        logAction(action, actionType, null, null, null, null);
    }
    
    /**
     * Registra una acción de auditoría con detalles del recurso
     */
    public void logAction(String action, AuditLog.ActionType actionType, 
                         String resourceType, Long resourceId, String resourceName) {
        logAction(action, actionType, resourceType, resourceId, resourceName, null);
    }
    
    /**
     * Registra una acción de auditoría con todos los detalles
     */
    public void logAction(String action, AuditLog.ActionType actionType, 
                         String resourceType, Long resourceId, String resourceName, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            
            // Obtener usuario actual
            String username = getCurrentUsername();
            auditLog.setUsername(username);
            
            // Información de la acción
            auditLog.setAction(action);
            auditLog.setActionType(actionType);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setResourceName(resourceName);
            auditLog.setDetails(details);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setStatus(AuditLog.Status.SUCCESS);
            
            // Obtener información de la request
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Si falla el log de auditoría, no debería afectar la operación principal
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }
    
    /**
     * Registra una acción fallida
     */
    public void logFailedAction(String action, AuditLog.ActionType actionType, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            
            String username = getCurrentUsername();
            auditLog.setUsername(username);
            auditLog.setAction(action);
            auditLog.setActionType(actionType);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setStatus(AuditLog.Status.FAILED);
            auditLog.setErrorMessage(errorMessage);
            
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría de fallo: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el nombre del usuario actual
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "system";
    }
    
    /**
     * Obtiene la request HTTP actual
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * Obtiene la IP real del cliente (considerando proxies)
     */
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
    
    // Métodos de conveniencia para acciones comunes
    public void logLogin(String username) {
        logAction("Inicio de sesión exitoso", AuditLog.ActionType.LOGIN);
    }
    
    public void logLogout(String username) {
        logAction("Cierre de sesión", AuditLog.ActionType.LOGOUT);
    }
    
    public void logTrackCreated(Long trackId, String trackName) {
        logAction("Pista creada", AuditLog.ActionType.CREATE, "Track", trackId, trackName);
    }
    
    public void logTrackUpdated(Long trackId, String trackName) {
        logAction("Pista actualizada", AuditLog.ActionType.UPDATE, "Track", trackId, trackName);
    }
    
    public void logTrackDeleted(Long trackId, String trackName) {
        logAction("Pista eliminada", AuditLog.ActionType.DELETE, "Track", trackId, trackName);
    }
    
    public void logUserCreated(Long userId, String username) {
        logAction("Usuario creado", AuditLog.ActionType.CREATE, "User", userId, username);
    }
    
    public void logUserUpdated(Long userId, String username) {
        logAction("Usuario actualizado", AuditLog.ActionType.UPDATE, "User", userId, username);
    }
    
    public void logUserDeleted(Long userId, String username) {
        logAction("Usuario eliminado", AuditLog.ActionType.DELETE, "User", userId, username);
    }
    
    public void logConfigurationChange(String configType) {
        logAction("Configuración modificada: " + configType, AuditLog.ActionType.CONFIGURE, "Configuration", null, configType);
    }
    
    public void logTrackExport(Long trackId, String trackName) {
        logAction("Pista exportada", AuditLog.ActionType.EXPORT, "Track", trackId, trackName);
    }
    
    public void logTrackImport(String trackName) {
        logAction("Pista importada", AuditLog.ActionType.IMPORT, "Track", null, trackName);
    }
}