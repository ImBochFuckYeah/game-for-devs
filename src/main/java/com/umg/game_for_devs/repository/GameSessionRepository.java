package com.umg.game_for_devs.repository;

import com.umg.game_for_devs.entity.GameSession;
import com.umg.game_for_devs.entity.Track;
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
 * Repositorio JPA para la entidad GameSession
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    /**
     * Buscar sesión por sessionId
     */
    Optional<GameSession> findBySessionId(String sessionId);
    
    /**
     * Buscar sesiones por estado
     */
    Page<GameSession> findByStatusOrderByStartTimeDesc(GameSession.GameStatus status, Pageable pageable);
    
    /**
     * Buscar sesiones por pista
     */
    Page<GameSession> findByTrackOrderByStartTimeDesc(Track track, Pageable pageable);
    
    /**
     * Buscar sesiones por rango de fechas
     */
    Page<GameSession> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Contar sesiones por estado
     */
    long countByStatus(GameSession.GameStatus status);
    
    /**
     * Contar sesiones por pista
     */
    long countByTrack(Track track);
    
    /**
     * Contar sesiones por rango de fechas
     */
    long countByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Obtener estadísticas de éxito por pista
     */
    @Query("SELECT gs.track.id, gs.track.name, " +
           "COUNT(gs) as total, " +
           "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successful, " +
           "AVG(gs.movesCount) as avgMoves " +
           "FROM GameSession gs " +
           "WHERE gs.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY gs.track.id, gs.track.name " +
           "ORDER BY successful DESC, total DESC")
    List<Object[]> getSuccessStatsByTrack(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtener estadísticas por día
     */
    @Query("SELECT DATE(gs.startTime) as day, " +
           "COUNT(gs) as total, " +
           "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successful, " +
           "SUM(CASE WHEN gs.status = 'FAILED' THEN 1 ELSE 0 END) as failed " +
           "FROM GameSession gs " +
           "WHERE gs.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(gs.startTime) " +
           "ORDER BY day")
    List<Object[]> getDailyStats(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtener estadísticas por dispositivo
     */
    @Query("SELECT gs.deviceType, " +
           "COUNT(gs) as total, " +
           "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successful " +
           "FROM GameSession gs " +
           "WHERE gs.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY gs.deviceType " +
           "ORDER BY total DESC")
    List<Object[]> getDeviceStats(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtener tiempo promedio de juego por pista
     */
    @Query("SELECT gs.track.name, " +
           "AVG(gs.executionTimeMs) as avgExecutionTime, " +
           "AVG(gs.movesCount) as avgMoves, " +
           "COUNT(gs) as sessions " +
           "FROM GameSession gs " +
           "WHERE gs.status = 'SUCCESS' AND gs.executionTimeMs IS NOT NULL " +
           "AND gs.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY gs.track.id, gs.track.name " +
           "ORDER BY avgExecutionTime")
    List<Object[]> getAverageTimesToComplete(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtener errores más comunes
     */
    @Query("SELECT gs.errorMessage, " +
           "COUNT(gs) as frequency " +
           "FROM GameSession gs " +
           "WHERE gs.status = 'FAILED' AND gs.errorMessage IS NOT NULL " +
           "AND gs.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY gs.errorMessage " +
           "ORDER BY frequency DESC")
    List<Object[]> getMostCommonErrors(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Buscar sesiones con múltiples filtros
     */
    @Query("SELECT gs FROM GameSession gs WHERE " +
           "(:status IS NULL OR gs.status = :status) AND " +
           "(:trackId IS NULL OR gs.track.id = :trackId) AND " +
           "(:deviceType IS NULL OR gs.deviceType = :deviceType) AND " +
           "gs.startTime BETWEEN :startDate AND :endDate " +
           "ORDER BY gs.startTime DESC")
    Page<GameSession> findByFilters(@Param("status") GameSession.GameStatus status,
                                   @Param("trackId") Long trackId,
                                   @Param("deviceType") String deviceType,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
    
    /**
     * Obtener sesiones más recientes
     */
    List<GameSession> findTop20ByOrderByStartTimeDesc();
    
    /**
     * Obtener estadísticas generales de rendimiento
     */
    @Query("SELECT " +
           "COUNT(gs) as totalSessions, " +
           "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulSessions, " +
           "SUM(CASE WHEN gs.status = 'FAILED' THEN 1 ELSE 0 END) as failedSessions, " +
           "AVG(gs.movesCount) as avgMoves, " +
           "AVG(gs.executionTimeMs) as avgExecutionTime " +
           "FROM GameSession gs " +
           "WHERE gs.startTime BETWEEN :startDate AND :endDate")
    List<Object[]> getGeneralStats(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Cuenta sesiones completadas exitosamente
     */
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.status = 'SUCCESS'")
    long countByCompletedTrue();
}