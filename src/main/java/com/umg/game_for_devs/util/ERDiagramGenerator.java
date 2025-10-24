package com.umg.game_for_devs.util;

import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Attribute;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationArguments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * Generador automático de diagrama ER en formato PlantUML
 * Solo se ejecuta en el perfil 'dev' para no interferir en producción
 */
@Component
@Profile("dev")
public class ERDiagramGenerator implements ApplicationRunner {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        generateERDiagram();
    }
    
    public void generateERDiagram() {
        try {
            StringBuilder plantuml = new StringBuilder();
            plantuml.append("@startuml\n");
            plantuml.append("!define ENTITY class\n");
            plantuml.append("!define PK <b><color:red>\n");
            plantuml.append("!define FK <color:blue>\n\n");
            
            Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
            
            // Generar cada entidad
            for (EntityType<?> entity : entities) {
                plantuml.append("ENTITY ").append(entity.getName().toLowerCase()).append(" {\n");
                
                // Atributos
                Set<Attribute<? super Object, ?>> attributes = entity.getAttributes();
                boolean first = true;
                for (Attribute<? super Object, ?> attribute : attributes) {
                    if (first) {
                        // Asumir que el primer atributo es PK
                        plantuml.append("  PK ").append(attribute.getName())
                               .append(" : ").append(getSimpleType(attribute.getJavaType())).append("\n");
                        plantuml.append("  --\n");
                        first = false;
                    } else {
                        String prefix = attribute.getName().toLowerCase().contains("id") && 
                                       !attribute.getName().equals("id") ? "FK " : "";
                        plantuml.append("  ").append(prefix).append(attribute.getName())
                               .append(" : ").append(getSimpleType(attribute.getJavaType())).append("\n");
                    }
                }
                plantuml.append("}\n\n");
            }
            
            // Relaciones básicas (puedes expandir esto)
            plantuml.append("tracks ||--o{ game_sessions : \"has many\"\n");
            
            plantuml.append("@enduml\n");
            
            // Guardar archivo
            try (FileWriter writer = new FileWriter("er-diagram.puml")) {
                writer.write(plantuml.toString());
                System.out.println("✅ Diagrama ER generado en: er-diagram.puml");
                System.out.println("💡 Para visualizar: https://www.planttext.com/");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generando diagrama ER: " + e.getMessage());
        }
    }
    
    private String getSimpleType(Class<?> type) {
        if (type == String.class) return "VARCHAR";
        if (type == Long.class || type == long.class) return "BIGINT";
        if (type == Integer.class || type == int.class) return "INTEGER";
        if (type == Boolean.class || type == boolean.class) return "BOOLEAN";
        if (type.getName().contains("LocalDateTime")) return "DATETIME";
        if (type.getName().contains("BigDecimal")) return "DECIMAL";
        return type.getSimpleName().toUpperCase();
    }
}