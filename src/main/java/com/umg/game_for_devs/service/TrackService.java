package com.umg.game_for_devs.service;

import com.umg.game_for_devs.entity.Track;
import com.umg.game_for_devs.repository.TrackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestión de pistas del juego
 */
@Service
@Transactional
public class TrackService {
    
    @Autowired
    private TrackRepository trackRepository;
    
    @Autowired
    private AuditService auditService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Obtener todas las pistas activas paginadas
     */
    public Page<Track> getAllTracks(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                                 Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return trackRepository.findAll(pageable);
    }
    
    /**
     * Obtener todas las pistas activas
     */
    public List<Track> getAllActiveTracks() {
        return trackRepository.findByIsActiveTrue();
    }
    
    /**
     * Obtener pista por ID
     */
    public Optional<Track> getTrackById(Long id) {
        return trackRepository.findById(id);
    }
    
    /**
     * Obtener una pista aleatoria activa
     */
    public Optional<Track> getRandomTrack() {
        return trackRepository.findRandomActiveTrack();
    }
    
    /**
     * Crear una nueva pista
     */
    public Track createTrack(Track track, String currentUsername) {
        // Validar que el nombre no exista
        if (trackRepository.existsByNameAndIsActiveTrue(track.getName())) {
            throw new RuntimeException("Ya existe una pista con ese nombre");
        }
        
        // Validar configuración del grid
        validateGridConfig(track.getGridConfig());
        
        // Establecer creado por
        track.setCreatedBy(currentUsername);
        
        // Guardar pista
        Track savedTrack = trackRepository.save(track);
        
        // Registrar en auditoría
        auditService.logTrackCreated(savedTrack.getId(), savedTrack.getName());
        
        return savedTrack;
    }
    
    /**
     * Actualizar una pista existente
     */
    public Track updateTrack(Long id, Track trackDetails, String currentUsername) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pista no encontrada"));
        
        // Validar nombre único (excluyendo la pista actual)
        if (!track.getName().equals(trackDetails.getName()) && 
            trackRepository.existsByNameAndIsActiveTrue(trackDetails.getName())) {
            throw new RuntimeException("Ya existe una pista con ese nombre");
        }
        
        // Validar configuración del grid
        validateGridConfig(trackDetails.getGridConfig());
        
        // Validar que no se desactive la última pista activa
        if (track.getIsActive() && !trackDetails.getIsActive()) {
            long activeTrackCount = trackRepository.countByIsActiveTrue();
            if (activeTrackCount <= 1) {
                throw new RuntimeException("No se puede desactivar la última pista activa");
            }
        }
        
        // Actualizar campos
        track.setName(trackDetails.getName());
        track.setGridConfig(trackDetails.getGridConfig());
        track.setStartX(trackDetails.getStartX());
        track.setStartY(trackDetails.getStartY());
        track.setStartDirection(trackDetails.getStartDirection());
        track.setDifficultyLevel(trackDetails.getDifficultyLevel());
        track.setDescription(trackDetails.getDescription());
        track.setIsActive(trackDetails.getIsActive());
        
        Track savedTrack = trackRepository.save(track);
        
        // Registrar en auditoría
        auditService.logTrackUpdated(savedTrack.getId(), savedTrack.getName());
        
        return savedTrack;
    }
    
    /**
     * Eliminar una pista (soft delete)
     */
    public void deleteTrack(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pista no encontrada"));
        
        // Verificar que quede al menos una pista activa
        long activeTrackCount = trackRepository.countByIsActiveTrue();
        if (activeTrackCount <= 1) {
            throw new RuntimeException("No se puede eliminar la última pista activa");
        }
        
        track.setIsActive(false);
        trackRepository.save(track);
        
        // Registrar en auditoría
        auditService.logTrackDeleted(track.getId(), track.getName());
    }
    
    /**
     * Reactivar una pista
     */
    public Track reactivateTrack(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pista no encontrada"));
        
        track.setIsActive(true);
        Track savedTrack = trackRepository.save(track);
        
        // Registrar en auditoría
        auditService.logAction("Pista reactivada", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.UPDATE, 
                              "Track", track.getId(), track.getName());
        
        return savedTrack;
    }
    
    /**
     * Buscar pistas por término
     */
    public List<Track> searchTracks(String searchTerm) {
        return trackRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(searchTerm);
    }
    
    /**
     * Obtener pistas por dificultad
     */
    public List<Track> getTracksByDifficulty(Integer difficulty) {
        return trackRepository.findByDifficultyLevelAndIsActiveTrueOrderByCreatedAtDesc(difficulty);
    }
    
    /**
     * Exportar pista a JSON
     */
    public String exportTrack(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pista no encontrada"));
        
        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("name", track.getName());
            exportData.put("gridConfig", track.getGridConfig());
            exportData.put("startX", track.getStartX());
            exportData.put("startY", track.getStartY());
            exportData.put("startDirection", track.getStartDirection());
            exportData.put("difficultyLevel", track.getDifficultyLevel());
            exportData.put("description", track.getDescription());
            exportData.put("exportVersion", "1.0");
            exportData.put("exportDate", java.time.LocalDateTime.now().toString());
            
            String jsonData = objectMapper.writeValueAsString(exportData);
            
            // Registrar en auditoría
            auditService.logTrackExport(track.getId(), track.getName());
            
            return jsonData;
        } catch (Exception e) {
            throw new RuntimeException("Error al exportar la pista: " + e.getMessage());
        }
    }
    
    /**
     * Importar pista desde archivo JSON
     */
    public Track importTrack(MultipartFile file, String currentUsername) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> trackData = objectMapper.readValue(content, Map.class);
            
            // Crear nueva pista desde los datos importados
            Track track = new Track();
            track.setName((String) trackData.get("name"));
            track.setGridConfig((String) trackData.get("gridConfig"));
            track.setStartX((Integer) trackData.get("startX"));
            track.setStartY((Integer) trackData.get("startY"));
            track.setStartDirection((String) trackData.get("startDirection"));
            track.setDifficultyLevel((Integer) trackData.get("difficultyLevel"));
            track.setDescription((String) trackData.get("description"));
            track.setCreatedBy(currentUsername);
            
            // Si ya existe una pista con el mismo nombre, agregar sufijo
            String originalName = track.getName();
            int suffix = 1;
            while (trackRepository.existsByNameAndIsActiveTrue(track.getName())) {
                track.setName(originalName + " (" + suffix + ")");
                suffix++;
            }
            
            // Validar configuración del grid
            validateGridConfig(track.getGridConfig());
            
            Track savedTrack = trackRepository.save(track);
            
            // Registrar en auditoría
            auditService.logTrackImport(savedTrack.getName());
            
            return savedTrack;
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al importar la pista: " + e.getMessage());
        }
    }
    
    /**
     * Obtener estadísticas de pistas
     */
    public TrackStats getTrackStats() {
        long totalTracks = trackRepository.countByIsActiveTrue();
        List<Object[]> difficultyStats = trackRepository.getTrackStatsByDifficulty();
        
        Map<Integer, Long> tracksByDifficulty = new HashMap<>();
        for (Object[] stat : difficultyStats) {
            tracksByDifficulty.put((Integer) stat[0], (Long) stat[1]);
        }
        
        return new TrackStats(totalTracks, tracksByDifficulty);
    }
    
    /**
     * Validar configuración del grid
     */
    private void validateGridConfig(String gridConfig) {
        try {
            int[][] grid = objectMapper.readValue(gridConfig, int[][].class);
            
            // Validar dimensiones (4 filas x 5 columnas)
            if (grid.length != 4) {
                throw new RuntimeException("El grid debe tener exactamente 4 filas");
            }
            
            for (int[] row : grid) {
                if (row.length != 5) {
                    throw new RuntimeException("Cada fila del grid debe tener exactamente 5 columnas");
                }
            }
            
            // Validar que solo contenga 0s y 1s
            for (int[] row : grid) {
                for (int cell : row) {
                    if (cell != 0 && cell != 1) {
                        throw new RuntimeException("Las celdas del grid solo pueden contener 0 o 1");
                    }
                }
            }
            
            // Validar que haya al menos una celda con valor 1 (camino)
            boolean hasPath = false;
            for (int[] row : grid) {
                for (int cell : row) {
                    if (cell == 1) {
                        hasPath = true;
                        break;
                    }
                }
                if (hasPath) break;
            }
            
            if (!hasPath) {
                throw new RuntimeException("El grid debe tener al menos una celda de camino (valor 1)");
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Formato de grid inválido: debe ser un array JSON válido");
        }
    }
    
    /**
     * Verificar si un nombre de pista está disponible
     */
    public boolean isTrackNameAvailable(String name) {
        return !trackRepository.existsByNameAndIsActiveTrue(name);
    }
    
    /**
     * Clase interna para estadísticas de pistas
     */
    public static class TrackStats {
        private final long totalTracks;
        private final Map<Integer, Long> tracksByDifficulty;
        
        public TrackStats(long totalTracks, Map<Integer, Long> tracksByDifficulty) {
            this.totalTracks = totalTracks;
            this.tracksByDifficulty = tracksByDifficulty;
        }
        
        public long getTotalTracks() { return totalTracks; }
        public Map<Integer, Long> getTracksByDifficulty() { return tracksByDifficulty; }
    }
}