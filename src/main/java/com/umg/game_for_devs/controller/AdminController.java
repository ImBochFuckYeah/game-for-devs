package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para el panel de administración
 * Requiere autenticación como ADMIN
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Dashboard principal del administrador
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Panel de Administración - Game For Devs");
        model.addAttribute("username", authentication.getName());
        
        // Registrar acceso al dashboard
        auditService.logAction("Acceso al dashboard administrativo", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        
        return "admin/dashboard";
    }
    
    /**
     * Gestión de usuarios administradores
     */
    @GetMapping("/users")
    public String users(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Gestión de Usuarios - Game For Devs");
        model.addAttribute("username", authentication.getName());
        
        auditService.logAction("Acceso a gestión de usuarios", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        
        return "admin/users";
    }
    
    /**
     * Gestión de pistas
     */
    @GetMapping("/tracks")
    public String tracks(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Gestión de Pistas - Game For Devs");
        model.addAttribute("username", authentication.getName());
        
        auditService.logAction("Acceso a gestión de pistas", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        
        return "admin/tracks";
    }
    
    /**
     * Bitácora de auditoría
     */
    @GetMapping("/audit")
    public String audit(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Bitácora de Auditoría - Game For Devs");
        model.addAttribute("username", authentication.getName());
        
        auditService.logAction("Acceso a bitácora de auditoría", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        
        return "admin/audit";
    }
    
    /**
     * Estadísticas de uso
     */
    @GetMapping("/statistics")
    public String statistics(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Estadísticas de Uso - Game For Devs");
        model.addAttribute("username", authentication.getName());
        
        auditService.logAction("Acceso a estadísticas de uso", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        
        return "admin/statistics";
    }
}