package com.umg.game_for_devs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador público para el juego Game For Devs
 * No requiere autenticación - acceso libre para todos los usuarios
 */
@Controller
public class GameController {
    
    /**
     * Página principal del juego - Redirige al juego
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
    public String game(Model model) {
        model.addAttribute("pageTitle", "Game For Devs - Codifica con Guali");
        return "game";
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
}