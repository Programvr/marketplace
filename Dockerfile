# Etapa 1: Build con Maven usando Java 25
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente
COPY src ./src

# Compilar la aplicación con Java 25
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con JRE 25
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Crear usuario no root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]