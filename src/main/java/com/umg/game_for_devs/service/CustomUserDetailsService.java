package com.umg.game_for_devs.service;

import com.umg.game_for_devs.entity.User;
import com.umg.game_for_devs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio personalizado para la autenticación de usuarios
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }
        
        // Actualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        return new CustomUserPrincipal(user);
    }
    
    /**
     * Clase interna que implementa UserDetails para Spring Security
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        @Override
        public String getUsername() {
            return user.getUsername();
        }
        
        @Override
        public String getPassword() {
            return user.getPassword();
        }
        
        @Override
        public List<GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            return authorities;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return user.getIsActive();
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return user.getIsActive();
        }
        
        // Métodos adicionales para acceder a información del usuario
        public User getUser() {
            return user;
        }
        
        public String getFullName() {
            return user.getFullName();
        }
        
        public String getEmail() {
            return user.getEmail();
        }
        
        public User.Role getRole() {
            return user.getRole();
        }
        
        public Long getUserId() {
            return user.getId();
        }
    }
}