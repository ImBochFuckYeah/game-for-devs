package com.umg.game_for_devs.service;

import com.umg.game_for_devs.entity.User;
import com.umg.game_for_devs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios administradores
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Obtener todos los usuarios activos paginados
     */
    public Page<User> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                                 Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return userRepository.findAll(pageable);
    }
    
    /**
     * Obtener todos los usuarios activos
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    /**
     * Obtener usuario por ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Obtener usuario por username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Crear un nuevo usuario
     */
    public User createUser(User user, String currentUsername) {
        // Validar que el username no exista
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Ya existe un usuario con ese nombre de usuario");
        }
        
        // Validar que el email no exista
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        
        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Establecer creado por
        user.setCreatedBy(currentUsername);
        
        // Guardar usuario
        User savedUser = userRepository.save(user);
        
        // Registrar en auditoría
        auditService.logUserCreated(savedUser.getId(), savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * Actualizar un usuario existente
     */
    public User updateUser(Long id, User userDetails, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar username único (excluyendo el usuario actual)
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsernameAndIdNot(userDetails.getUsername(), id)) {
            throw new RuntimeException("Ya existe un usuario con ese nombre de usuario");
        }
        
        // Validar email único (excluyendo el usuario actual)
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmailAndIdNot(userDetails.getEmail(), id)) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        
        // Actualizar campos
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setFullName(userDetails.getFullName());
        user.setRole(userDetails.getRole());
        user.setIsActive(userDetails.getIsActive());
        
        // Solo actualizar contraseña si se proporciona una nueva
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        
        // Registrar en auditoría
        auditService.logUserUpdated(savedUser.getId(), savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * Eliminar un usuario (soft delete)
     */
    public void deleteUser(Long id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // No permitir que un usuario se elimine a sí mismo
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("No puedes eliminar tu propio usuario");
        }
        
        // Verificar que quede al menos un administrador activo
        long activeAdminCount = userRepository.countByRoleAndIsActiveTrue(User.Role.ADMIN);
        if (activeAdminCount <= 1 && user.getRole() == User.Role.ADMIN && user.getIsActive()) {
            throw new RuntimeException("No se puede eliminar el último administrador activo");
        }
        
        user.setIsActive(false);
        userRepository.save(user);
        
        // Registrar en auditoría
        auditService.logUserDeleted(user.getId(), user.getUsername());
    }
    
    /**
     * Reactivar un usuario
     */
    public User reactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        
        // Registrar en auditoría
        auditService.logAction("Usuario reactivado", 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.UPDATE, 
                              "User", user.getId(), user.getUsername());
        
        return savedUser;
    }
    
    /**
     * Cambiar contraseña de un usuario
     */
    public void changePassword(Long id, String newPassword, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Registrar en auditoría
        auditService.logAction("Contraseña cambiada para usuario: " + user.getUsername(), 
                              com.umg.game_for_devs.entity.AuditLog.ActionType.UPDATE, 
                              "User", user.getId(), user.getUsername());
    }
    
    /**
     * Buscar usuarios por término
     */
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByFullNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(searchTerm);
    }
    
    /**
     * Obtener estadísticas de usuarios
     */
    public UserStats getUserStats() {
        long totalUsers = userRepository.countByIsActiveTrue();
        long adminUsers = userRepository.countByRoleAndIsActiveTrue(User.Role.ADMIN);
        long superAdminUsers = userRepository.countByRoleAndIsActiveTrue(User.Role.SUPER_ADMIN);
        
        return new UserStats(totalUsers, adminUsers, superAdminUsers);
    }
    
    /**
     * Verificar si un username está disponible
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * Verificar si un email está disponible
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    /**
     * Clase interna para estadísticas de usuarios
     */
    public static class UserStats {
        private final long totalUsers;
        private final long adminUsers;
        private final long superAdminUsers;
        
        public UserStats(long totalUsers, long adminUsers, long superAdminUsers) {
            this.totalUsers = totalUsers;
            this.adminUsers = adminUsers;
            this.superAdminUsers = superAdminUsers;
        }
        
        public long getTotalUsers() { return totalUsers; }
        public long getAdminUsers() { return adminUsers; }
        public long getSuperAdminUsers() { return superAdminUsers; }
    }
}