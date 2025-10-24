# Dockerfile para Game For Devs
# Usar multi-stage build para optimizar la imagen final
FROM eclipse-temurin:17-jdk-alpine AS builder

# Etiquetas de metadatos
LABEL maintainer="game-for-devs"
LABEL description="Contenedor Docker para Game For Devs - Spring Boot Application"
LABEL version="1.0"

# Instalar dependencias necesarias
RUN apk add --no-cache wget

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml y el wrapper de Maven
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Otorga permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descarga las dependencias (esto se cachea si el pom.xml no cambia)
RUN ./mvnw dependency:resolve -B || echo "Continuing with available dependencies"

# Copia el código fuente
COPY src ./src

# Construye la aplicación
RUN ./mvnw clean package -DskipTests -B

# Segunda etapa: imagen de runtime
FROM eclipse-temurin:17-jre-alpine

# Instalar wget para healthcheck
RUN apk add --no-cache wget

# Directorio de trabajo
WORKDIR /app

# Copia solo el JAR construido desde la etapa anterior
COPY --from=builder /app/target/game-for-devs-*.jar app.jar

# Expone el puerto 8080
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Healthcheck para verificar que la aplicación esté funcionando
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1