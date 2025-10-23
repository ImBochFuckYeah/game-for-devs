package com.umg.game_for_devs.repository;

import com.umg.game_for_devs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Buscar usuario por nombre de usuario
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Buscar usuario por email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Buscar usuarios activos
     */
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Buscar usuarios por rol
     */
    List<User> findByRoleAndIsActiveTrueOrderByCreatedAtDesc(User.Role role);
    
    /**
     * Verificar si existe usuario por username
     */
    boolean existsByUsername(String username);
    
    /**
     * Verificar si existe usuario por email
     */
    boolean existsByEmail(String email);
    
    /**
     * Buscar por nombre completo que contenga
     */
    List<User> findByFullNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String fullName);
    
    /**
     * Contar usuarios activos
     */
    long countByIsActiveTrue();
    
    /**
     * Contar usuarios por rol
     */
    long countByRoleAndIsActiveTrue(User.Role role);
    
    /**
     * Buscar usuarios que han iniciado sesión después de una fecha
     */
    List<User> findByLastLoginAfterAndIsActiveTrueOrderByLastLoginDesc(LocalDateTime date);
    
    /**
     * Buscar usuarios creados por un administrador específico
     */
    List<User> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Obtener estadísticas de usuarios por rol
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.role")
    List<Object[]> getUserStatsByRole();
    
    /**
     * Buscar usuarios más recientes
     */
    List<User> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Verificar existencia para validación de unicidad (excluyendo el propio usuario)
     */
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
}