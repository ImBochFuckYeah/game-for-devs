package com.umg.game_for_devs.repository;

import com.umg.game_for_devs.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Track
 */
@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    
    /**
     * Buscar pistas activas
     */
    List<Track> findByIsActiveTrue();
    
    /**
     * Buscar pista por nombre (activa)
     */
    Optional<Track> findByNameAndIsActiveTrue(String name);
    
    /**
     * Buscar pistas por nivel de dificultad
     */
    List<Track> findByDifficultyLevelAndIsActiveTrueOrderByCreatedAtDesc(Integer difficultyLevel);
    
    /**
     * Buscar pistas creadas por un usuario específico
     */
    List<Track> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Obtener una pista aleatoria activa
     */
    @Query(value = "SELECT * FROM tracks WHERE is_active = true ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Track> findRandomActiveTrack();
    
    /**
     * Contar pistas activas
     */
    long countByIsActiveTrue();
    
    /**
     * Buscar por nombre que contenga (búsqueda parcial)
     */
    List<Track> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String name);
    
    /**
     * Obtener estadísticas de pistas por dificultad
     */
    @Query("SELECT t.difficultyLevel, COUNT(t) FROM Track t WHERE t.isActive = true GROUP BY t.difficultyLevel ORDER BY t.difficultyLevel")
    List<Object[]> getTrackStatsByDifficulty();
    
    /**
     * Verificar si existe una pista con el mismo nombre (para validación)
     */
    boolean existsByNameAndIsActiveTrue(String name);
    
    /**
     * Buscar pistas más recientes
     */
    List<Track> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
}