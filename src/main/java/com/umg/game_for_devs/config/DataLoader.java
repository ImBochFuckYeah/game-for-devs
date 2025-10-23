package com.umg.game_for_devs.config;

import com.umg.game_for_devs.entity.User;
import com.umg.game_for_devs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cargador de datos iniciales para el sistema
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminUser();
    }

    /**
     * Crear usuario administrador por defecto si no existe
     */
    private void createDefaultAdminUser() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@gamefordevs.com");
            admin.setFullName("Administrador del Sistema");
            admin.setRole(User.Role.ADMIN);
            admin.setIsActive(true);
            
            userRepository.save(admin);
            System.out.println("✅ Usuario administrador creado:");
            System.out.println("   Usuario: admin");
            System.out.println("   Contraseña: admin123");
            System.out.println("   Email: admin@gamefordevs.com");
        }
    }
}