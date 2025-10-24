package com.umg.game_for_devs.controller;

import com.umg.game_for_devs.service.AuditService;
import com.umg.game_for_devs.repository.UserRepository;
import com.umg.game_for_devs.repository.TrackRepository;
import com.umg.game_for_devs.repository.GameSessionRepository;
import com.umg.game_for_devs.repository.AuditLogRepository;
import com.umg.game_for_devs.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controlador para el panel de administración
 * Requiere autenticación como ADMIN
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TrackRepository trackRepository;
    
    @Autowired
    private GameSessionRepository gameSessionRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Redirigir /admin a /admin/dashboard
     */
    @GetMapping({"", "/"})
    public String index() {
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Dashboard principal del administrador
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Panel de Administración - Game For Devs");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        
        try {
            // Registrar acceso al dashboard
            auditService.logAction("Acceso al dashboard administrativo", 
                                  com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        } catch (Exception e) {
            // Si hay error en auditoría, no afectar la carga de la página
            System.err.println("Error registrando auditoría: " + e.getMessage());
        }
        
        // Cargar estadísticas dinámicas
        loadDashboardStatistics(model);
        
        return "admin/dashboard";
    }
    
    /**
     * Cargar estadísticas del dashboard desde la base de datos
     */
    private void loadDashboardStatistics(Model model) {
        try {
            // Obtener estadísticas básicas
            long totalUsers = userRepository.count();
            long totalTracks = trackRepository.count();
            long totalGamesPlayed = gameSessionRepository.count();
            long completedGames = gameSessionRepository.countByCompleted(true);
            long totalAuditEvents = auditLogRepository.count();
            
            // Calcular porcentaje de completación
            double completionPercentage = totalGamesPlayed > 0 ? 
                (completedGames * 100.0) / totalGamesPlayed : 0;
            
            // Agregar al modelo
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalTracks", totalTracks);
            model.addAttribute("completedGames", completedGames);
            model.addAttribute("totalGamesPlayed", totalGamesPlayed);
            model.addAttribute("completionPercentage", Math.round(completionPercentage));
            model.addAttribute("totalAuditEvents", totalAuditEvents);
            
            // Cargar actividad reciente (últimos 10 eventos de auditoría)
            List<AuditLog> recentActivity = auditLogRepository.findTop10ByOrderByTimestampDesc();
            model.addAttribute("recentActivity", recentActivity);
            
            // Datos para gráfico de actividad (últimos 7 días)
            loadActivityChartData(model);
            
        } catch (Exception e) {
            System.err.println("Error cargando estadísticas del dashboard: " + e.getMessage());
            // Valores por defecto en caso de error
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalTracks", 0);
            model.addAttribute("completedGames", 0);
            model.addAttribute("completionPercentage", 0);
            model.addAttribute("totalAuditEvents", 0);
        }
    }
    
    /**
     * Cargar datos para el gráfico de actividad
     */
    private void loadActivityChartData(Model model) {
        try {
            // Crear estructura de datos para los últimos 7 días
            String[] labels = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
            int[] data = new int[7];
            
            // Obtener fecha de hace 7 días
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            
            // Contar sesiones por día (simulado por ahora)
            for (int i = 0; i < 7; i++) {
                LocalDateTime dayStart = sevenDaysAgo.plusDays(i).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime dayEnd = dayStart.plusDays(1);
                
                // Contar eventos de auditoría del día como proxy de actividad
                long dayActivity = auditLogRepository.countByTimestampBetween(dayStart, dayEnd);
                data[i] = (int) dayActivity;
            }
            
            // Crear mapa para el gráfico
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("labels", labels);
            activityData.put("data", data);
            
            model.addAttribute("activityData", activityData);
            
        } catch (Exception e) {
            System.err.println("Error cargando datos de actividad: " + e.getMessage());
            // Datos por defecto
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("labels", new String[]{"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"});
            defaultData.put("data", new int[]{0, 0, 0, 0, 0, 0, 0});
            model.addAttribute("activityData", defaultData);
        }
    }
    
    /**
     * Gestión de usuarios administradores
     */
    @GetMapping("/users")
    public String users(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Gestión de Usuarios - Game For Devs");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        
        try {
            auditService.logAction("Acceso a gestión de usuarios", 
                                  com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        } catch (Exception e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
        }
        
        return "admin/users";
    }
    
    /**
     * Gestión de pistas
     */
    @GetMapping("/tracks")
    public String tracks(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Gestión de Pistas - Game For Devs");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        
        try {
            auditService.logAction("Acceso a gestión de pistas", 
                                  com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        } catch (Exception e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
        }
        
        return "admin/tracks";
    }

    /**
     * Bitácora de auditoría
     */
    @GetMapping("/audit")
    public String audit(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Bitácora de Auditoría - Game For Devs");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        
        try {
            auditService.logAction("Acceso a bitácora de auditoría", 
                                  com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        } catch (Exception e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
        }
        
        return "admin/audit";
    }

    /**
     * Estadísticas de uso
     */
    @GetMapping("/statistics")
    public String statistics(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Estadísticas de Uso - Game For Devs");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        
        try {
            auditService.logAction("Acceso a estadísticas de uso", 
                                  com.umg.game_for_devs.entity.AuditLog.ActionType.READ);
        } catch (Exception e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
        }
        
        return "admin/statistics";
    }
}