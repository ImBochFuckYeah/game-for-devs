package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador para la autenticación de administradores
 */
@Controller
public class AuthController {
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Página de login para administradores
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        model.addAttribute("pageTitle", "Acceso Administrativo - Game For Devs");
        
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión exitosamente");
        }
        
        return "auth/login";
    }
    
    /**
     * Logout manual (GET) para casos especiales
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            // Registrar logout en auditoría
            auditService.logLogout(authentication.getName());
            
            // Cerrar sesión
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        return "redirect:/game?logout=true";
    }
}