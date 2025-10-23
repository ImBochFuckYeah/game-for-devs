package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la configuración de pistas
 * Requiere autenticación como ADMIN
 */
@Controller
@RequestMapping("/config")
public class ConfigController {
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Interfaz de configuración de pistas
     */
    @GetMapping("/tracks")
    public String configTracks(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Configurar Pistas - Game For Devs");
        model.addAttribute("username", authentication.getName());
        
        auditService.logAction("Acceso a configuración de pistas", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        
        return "config/tracks";
    }
    
    /**
     * Redirige /config a /config/tracks
     */
    @GetMapping("")
    public String config() {
        return "redirect:/config/tracks";
    }
}