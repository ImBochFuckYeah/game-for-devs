package com.umg.game_for_devs.repository;

import com.umg.game_for_devs.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad AuditLog
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Buscar logs por usuario
     */
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);
    
    /**
     * Buscar logs por tipo de acción
     */
    Page<AuditLog> findByActionTypeOrderByTimestampDesc(AuditLog.ActionType actionType, Pageable pageable);
    
    /**
     * Buscar logs por rango de fechas
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Buscar logs por usuario y rango de fechas
     */
    Page<AuditLog> findByUsernameAndTimestampBetweenOrderByTimestampDesc(
            String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Buscar logs por tipo de recurso
     */
    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);
    
    /**
     * Buscar logs por estado
     */
    Page<AuditLog> findByStatusOrderByTimestampDesc(AuditLog.Status status, Pageable pageable);
    
    /**
     * Buscar logs más recientes
     */
    List<AuditLog> findTop20ByOrderByTimestampDesc();
    
    /**
     * Contar logs por usuario
     */
    long countByUsername(String username);
    
    /**
     * Contar logs por tipo de acción
     */
    long countByActionType(AuditLog.ActionType actionType);
    
    /**
     * Contar logs por rango de fechas
     */
    long countByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Obtener estadísticas de acciones por usuario
     */
    @Query("SELECT a.username, a.actionType, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.username, a.actionType " +
           "ORDER BY a.username, a.actionType")
    List<Object[]> getActionStatsByUser(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtener estadísticas de acciones por día
     */
    @Query("SELECT DATE(a.timestamp) as day, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(a.timestamp) " +
           "ORDER BY day")
    List<Object[]> getActionStatsByDay(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtener estadísticas de recursos más modificados
     */
    @Query("SELECT a.resourceType, a.resourceName, COUNT(a) FROM AuditLog a " +
           "WHERE a.resourceType IS NOT NULL AND a.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.resourceType, a.resourceName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getMostModifiedResources(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Buscar logs con filtros múltiples
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:username IS NULL OR a.username = :username) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByFilters(@Param("username") String username,
                                @Param("actionType") AuditLog.ActionType actionType,
                                @Param("resourceType") String resourceType,
                                @Param("status") AuditLog.Status status,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);
    
    /**
     * Obtener los 10 eventos más recientes
     */
    List<AuditLog> findTop10ByOrderByTimestampDesc();
    
    /**
     * Contar usuarios únicos activos después de cierta fecha
     */
    @Query("SELECT COUNT(DISTINCT a.username) FROM AuditLog a WHERE a.timestamp > :timestamp")
    long countDistinctUsernameByTimestampAfter(@Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Encontrar la entrada más reciente
     */
    Optional<AuditLog> findFirstByOrderByTimestampDesc();
    
    /**
     * Obtener actividad diaria de auditoría (para gráfico del dashboard)
     */
    @Query("SELECT DATE(a.timestamp) as auditDate, COUNT(a) as auditCount " +
           "FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(a.timestamp) " +
           "ORDER BY auditDate")
    List<Object[]> findDailyActivityBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
}