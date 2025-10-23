package com.umg.game_for_devs.service;

import com.umg.game_for_devs.entity.GameSession;
import com.umg.game_for_devs.repository.AuditLogRepository;
import com.umg.game_for_devs.repository.GameSessionRepository;
import com.umg.game_for_devs.repository.TrackRepository;
import com.umg.game_for_devs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar estadísticas del sistema
 */
@Service
public class StatisticsService {
    
    @Autowired
    private GameSessionRepository gameSessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TrackRepository trackRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Obtener estadísticas generales del dashboard
     */
    public DashboardStats getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        
        // Estadísticas básicas
        long totalUsers = userRepository.countByIsActiveTrue();
        long totalTracks = trackRepository.countByIsActiveTrue();
        
        // Sesiones de hoy
        long sessionsToday = gameSessionRepository.countByStartTimeBetween(startOfToday, now);
        
        // Sesiones de la semana
        long sessionsThisWeek = gameSessionRepository.countByStartTimeBetween(startOfWeek, now);
        
        // Tasa de éxito general
        long successfulSessions = gameSessionRepository.countByStatus(GameSession.GameStatus.SUCCESS);
        long totalSessions = gameSessionRepository.count();
        double successRate = totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0;
        
        // Actividad reciente (auditoría)
        long auditEntriesThisWeek = auditLogRepository.countByTimestampBetween(startOfWeek, now);
        
        return new DashboardStats(
            totalUsers,
            totalTracks,
            sessionsToday,
            sessionsThisWeek,
            successRate,
            auditEntriesThisWeek
        );
    }
    
    /**
     * Obtener estadísticas de actividad por días
     */
    public List<DailyActivityStats> getDailyActivityStats(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> dailyStats = gameSessionRepository.getDailyStats(startDate, endDate);
        
        return dailyStats.stream()
            .map(stat -> new DailyActivityStats(
                stat[0].toString(), // day
                ((Number) stat[1]).longValue(), // total
                ((Number) stat[2]).longValue(), // successful
                ((Number) stat[3]).longValue()  // failed
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas de éxito por pista
     */
    public List<TrackSuccessStats> getTrackSuccessStats() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1); // Último mes
        
        List<Object[]> trackStats = gameSessionRepository.getSuccessStatsByTrack(startDate, endDate);
        
        return trackStats.stream()
            .map(stat -> new TrackSuccessStats(
                ((Number) stat[0]).longValue(), // trackId
                (String) stat[1], // trackName
                ((Number) stat[2]).longValue(), // total
                ((Number) stat[3]).longValue(), // successful
                stat[4] != null ? ((Number) stat[4]).doubleValue() : 0.0 // avgMoves
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas por dispositivo
     */
    public List<DeviceStats> getDeviceStats() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1); // Último mes
        
        List<Object[]> deviceStats = gameSessionRepository.getDeviceStats(startDate, endDate);
        
        return deviceStats.stream()
            .map(stat -> new DeviceStats(
                (String) stat[0], // deviceType
                ((Number) stat[1]).longValue(), // total
                ((Number) stat[2]).longValue()  // successful
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener tiempos promedio de finalización
     */
    public List<TrackTimeStats> getAverageCompletionTimes() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1); // Último mes
        
        List<Object[]> timeStats = gameSessionRepository.getAverageTimesToComplete(startDate, endDate);
        
        return timeStats.stream()
            .map(stat -> new TrackTimeStats(
                (String) stat[0], // trackName
                stat[1] != null ? ((Number) stat[1]).longValue() : 0L, // avgExecutionTime
                stat[2] != null ? ((Number) stat[2]).doubleValue() : 0.0, // avgMoves
                ((Number) stat[3]).longValue() // sessions
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener errores más comunes
     */
    public List<ErrorStats> getMostCommonErrors() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1); // Último mes
        
        List<Object[]> errorStats = gameSessionRepository.getMostCommonErrors(startDate, endDate);
        
        return errorStats.stream()
            .limit(10) // Top 10 errores
            .map(stat -> new ErrorStats(
                (String) stat[0], // errorMessage
                ((Number) stat[1]).longValue() // frequency
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas de auditoría por usuario
     */
    public List<UserAuditStats> getUserAuditStats() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1); // Último mes
        
        List<Object[]> auditStats = auditLogRepository.getActionStatsByUser(startDate, endDate);
        
        Map<String, Map<String, Long>> userActionMap = new HashMap<>();
        
        for (Object[] stat : auditStats) {
            String username = (String) stat[0];
            String actionType = stat[1].toString();
            Long count = ((Number) stat[2]).longValue();
            
            userActionMap.computeIfAbsent(username, k -> new HashMap<>()).put(actionType, count);
        }
        
        return userActionMap.entrySet().stream()
            .map(entry -> new UserAuditStats(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas generales del sistema
     */
    public SystemOverviewStats getSystemOverviewStats() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1); // Último mes
        
        List<Object[]> generalStats = gameSessionRepository.getGeneralStats(startDate, endDate);
        
        if (!generalStats.isEmpty()) {
            Object[] stats = generalStats.get(0);
            return new SystemOverviewStats(
                ((Number) stats[0]).longValue(), // totalSessions
                ((Number) stats[1]).longValue(), // successfulSessions
                ((Number) stats[2]).longValue(), // failedSessions
                stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0, // avgMoves
                stats[4] != null ? ((Number) stats[4]).longValue() : 0L // avgExecutionTime
            );
        }
        
        return new SystemOverviewStats(0L, 0L, 0L, 0.0, 0L);
    }
    
    /**
     * Generar reporte completo del sistema
     */
    public SystemReport generateSystemReport() {
        return new SystemReport(
            LocalDateTime.now(),
            getDashboardStats(),
            getDailyActivityStats(30),
            getTrackSuccessStats(),
            getDeviceStats(),
            getAverageCompletionTimes(),
            getMostCommonErrors(),
            getUserAuditStats(),
            getSystemOverviewStats()
        );
    }
    
    // Clases internas para estadísticas
    
    public static class DashboardStats {
        private final long totalUsers;
        private final long totalTracks;
        private final long sessionsToday;
        private final long sessionsThisWeek;
        private final double successRate;
        private final long auditEntriesThisWeek;
        
        public DashboardStats(long totalUsers, long totalTracks, long sessionsToday, 
                            long sessionsThisWeek, double successRate, long auditEntriesThisWeek) {
            this.totalUsers = totalUsers;
            this.totalTracks = totalTracks;
            this.sessionsToday = sessionsToday;
            this.sessionsThisWeek = sessionsThisWeek;
            this.successRate = successRate;
            this.auditEntriesThisWeek = auditEntriesThisWeek;
        }
        
        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getTotalTracks() { return totalTracks; }
        public long getSessionsToday() { return sessionsToday; }
        public long getSessionsThisWeek() { return sessionsThisWeek; }
        public double getSuccessRate() { return successRate; }
        public long getAuditEntriesThisWeek() { return auditEntriesThisWeek; }
    }
    
    public static class DailyActivityStats {
        private final String date;
        private final long totalSessions;
        private final long successfulSessions;
        private final long failedSessions;
        
        public DailyActivityStats(String date, long totalSessions, long successfulSessions, long failedSessions) {
            this.date = date;
            this.totalSessions = totalSessions;
            this.successfulSessions = successfulSessions;
            this.failedSessions = failedSessions;
        }
        
        // Getters
        public String getDate() { return date; }
        public long getTotalSessions() { return totalSessions; }
        public long getSuccessfulSessions() { return successfulSessions; }
        public long getFailedSessions() { return failedSessions; }
        public double getSuccessRate() {
            return totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0;
        }
    }
    
    public static class TrackSuccessStats {
        private final long trackId;
        private final String trackName;
        private final long totalSessions;
        private final long successfulSessions;
        private final double avgMoves;
        
        public TrackSuccessStats(long trackId, String trackName, long totalSessions, 
                               long successfulSessions, double avgMoves) {
            this.trackId = trackId;
            this.trackName = trackName;
            this.totalSessions = totalSessions;
            this.successfulSessions = successfulSessions;
            this.avgMoves = avgMoves;
        }
        
        // Getters
        public long getTrackId() { return trackId; }
        public String getTrackName() { return trackName; }
        public long getTotalSessions() { return totalSessions; }
        public long getSuccessfulSessions() { return successfulSessions; }
        public double getAvgMoves() { return avgMoves; }
        public double getSuccessRate() {
            return totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0;
        }
    }
    
    public static class DeviceStats {
        private final String deviceType;
        private final long totalSessions;
        private final long successfulSessions;
        
        public DeviceStats(String deviceType, long totalSessions, long successfulSessions) {
            this.deviceType = deviceType;
            this.totalSessions = totalSessions;
            this.successfulSessions = successfulSessions;
        }
        
        // Getters
        public String getDeviceType() { return deviceType; }
        public long getTotalSessions() { return totalSessions; }
        public long getSuccessfulSessions() { return successfulSessions; }
        public double getSuccessRate() {
            return totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0;
        }
    }
    
    public static class TrackTimeStats {
        private final String trackName;
        private final long avgExecutionTimeMs;
        private final double avgMoves;
        private final long totalSessions;
        
        public TrackTimeStats(String trackName, long avgExecutionTimeMs, double avgMoves, long totalSessions) {
            this.trackName = trackName;
            this.avgExecutionTimeMs = avgExecutionTimeMs;
            this.avgMoves = avgMoves;
            this.totalSessions = totalSessions;
        }
        
        // Getters
        public String getTrackName() { return trackName; }
        public long getAvgExecutionTimeMs() { return avgExecutionTimeMs; }
        public double getAvgMoves() { return avgMoves; }
        public long getTotalSessions() { return totalSessions; }
        public double getAvgExecutionTimeSeconds() { return avgExecutionTimeMs / 1000.0; }
    }
    
    public static class ErrorStats {
        private final String errorMessage;
        private final long frequency;
        
        public ErrorStats(String errorMessage, long frequency) {
            this.errorMessage = errorMessage;
            this.frequency = frequency;
        }
        
        // Getters
        public String getErrorMessage() { return errorMessage; }
        public long getFrequency() { return frequency; }
    }
    
    public static class UserAuditStats {
        private final String username;
        private final Map<String, Long> actionCounts;
        
        public UserAuditStats(String username, Map<String, Long> actionCounts) {
            this.username = username;
            this.actionCounts = actionCounts;
        }
        
        // Getters
        public String getUsername() { return username; }
        public Map<String, Long> getActionCounts() { return actionCounts; }
        public long getTotalActions() {
            return actionCounts.values().stream().mapToLong(Long::longValue).sum();
        }
    }
    
    public static class SystemOverviewStats {
        private final long totalSessions;
        private final long successfulSessions;
        private final long failedSessions;
        private final double avgMoves;
        private final long avgExecutionTimeMs;
        
        public SystemOverviewStats(long totalSessions, long successfulSessions, long failedSessions, 
                                 double avgMoves, long avgExecutionTimeMs) {
            this.totalSessions = totalSessions;
            this.successfulSessions = successfulSessions;
            this.failedSessions = failedSessions;
            this.avgMoves = avgMoves;
            this.avgExecutionTimeMs = avgExecutionTimeMs;
        }
        
        // Getters
        public long getTotalSessions() { return totalSessions; }
        public long getSuccessfulSessions() { return successfulSessions; }
        public long getFailedSessions() { return failedSessions; }
        public double getAvgMoves() { return avgMoves; }
        public long getAvgExecutionTimeMs() { return avgExecutionTimeMs; }
        public double getSuccessRate() {
            return totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0;
        }
    }
    
    public static class SystemReport {
        private final LocalDateTime generatedAt;
        private final DashboardStats dashboardStats;
        private final List<DailyActivityStats> dailyActivity;
        private final List<TrackSuccessStats> trackSuccess;
        private final List<DeviceStats> deviceStats;
        private final List<TrackTimeStats> timeStats;
        private final List<ErrorStats> errorStats;
        private final List<UserAuditStats> auditStats;
        private final SystemOverviewStats overviewStats;
        
        public SystemReport(LocalDateTime generatedAt, DashboardStats dashboardStats, 
                           List<DailyActivityStats> dailyActivity, List<TrackSuccessStats> trackSuccess,
                           List<DeviceStats> deviceStats, List<TrackTimeStats> timeStats,
                           List<ErrorStats> errorStats, List<UserAuditStats> auditStats,
                           SystemOverviewStats overviewStats) {
            this.generatedAt = generatedAt;
            this.dashboardStats = dashboardStats;
            this.dailyActivity = dailyActivity;
            this.trackSuccess = trackSuccess;
            this.deviceStats = deviceStats;
            this.timeStats = timeStats;
            this.errorStats = errorStats;
            this.auditStats = auditStats;
            this.overviewStats = overviewStats;
        }
        
        // Getters
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public DashboardStats getDashboardStats() { return dashboardStats; }
        public List<DailyActivityStats> getDailyActivity() { return dailyActivity; }
        public List<TrackSuccessStats> getTrackSuccess() { return trackSuccess; }
        public List<DeviceStats> getDeviceStats() { return deviceStats; }
        public List<TrackTimeStats> getTimeStats() { return timeStats; }
        public List<ErrorStats> getErrorStats() { return errorStats; }
        public List<UserAuditStats> getAuditStats() { return auditStats; }
        public SystemOverviewStats getOverviewStats() { return overviewStats; }
    }
}