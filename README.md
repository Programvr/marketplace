# Marketplace API - ConectaClick

API RESTful para un marketplace con arquitectura hexagonal, desarrollada con Spring Boot, PostgreSQL y MongoDB.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Configuración](#configuración)
- [Endpoints API](#endpoints-api)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Pruebas](#pruebas)
- [Despliegue con Docker](#despliegue-con-docker)
- [Solución de Problemas](#solución-de-problemas)

## 🚀 Características

- ✅ **Arquitectura Hexagonal (Clean Architecture)** - Separación clara de responsabilidades
- ✅ **Spring Boot 3.2.0** - Framework moderno y eficiente
- ✅ **PostgreSQL** - Base de datos transaccional para datos críticos
- ✅ **MongoDB** - Base de datos NoSQL para logs y carritos temporales
- ✅ **JPA/Hibernate** - ORM para PostgreSQL
- ✅ **Spring Security** - Seguridad y autenticación (configurable)
- ✅ **Docker** - Contenedorización completa
- ✅ **Health Checks** - Monitoreo de servicios
- ✅ **Logging Estructurado** - Logs en MongoDB para auditoría
- ✅ **Validación de Datos** - Bean Validation y validaciones personalizadas

## 🛠 Tecnologías

| Tecnología | Versión | Propósito |
|------------|--------|-----------|
| Java | 25     | Lenguaje principal |
| Spring Boot | 4.0.5 | Framework principal |
| PostgreSQL | 16     | Base de datos transaccional |
| MongoDB | 7.0    | Base de datos NoSQL |
| Maven | 3.9+   | Gestor de dependencias |
| Docker | 24.0+  | Contenedorización |
| Lombok | 1.18.30 | Reducción de código boilerplate |
| MapStruct | 1.5.5  | Mapeo entre capas |

## 🏗 Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Puertos y Adaptadores)**:

┌─────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ │
│ │ Web/REST │ │ Persistence │ │ Security │ │
│ │ Controllers │ │ (JPA/Mongo)│ │ (JWT) │ │
│ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ │
│ │ │ │ │
└─────────┼──────────────────┼──────────────────┼──────────────┘
│ │ │
▼ ▼ ▼
┌─────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Use Cases (Services) │ │
│ │ - CreateProductService │ │
│ │ - LoggingService │ │
│ └──────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ DOMAIN LAYER │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ - Entities (Product, User) │ │
│ │ - Value Objects │ │
│ │ - Domain Exceptions │ │
│ │ - Business Rules │ │
│ └──────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘


## 📋 Requisitos Previos

### Software Necesario
- **Java 25** 
- **Maven 3.9+**
- **Docker** y **Docker Compose** (para despliegue)
- **Git** (para clonar el repositorio)

### Herramientas Recomendadas
- **IntelliJ IDEA** / **VS Code** para desarrollo
- **pgAdmin** / **DBeaver** para PostgreSQL
- **MongoDB Compass** / **Robo 3T** para MongoDB
- **Postman** / **Insomnia** para probar APIs

## 🔧 Instalación y Ejecución

### 1. Clonar el repositorio

```bash
  git clone https://github.com/Programvr/marketplace.git
cd marketplace-api

```

### Iniciar PostgreSQL y MongoDB
```bash
  docker-compose up -d
```

### Verificar estado de los servicios
```bash
  docker-compose ps
```

### Ver logs en tiempo real
```bash
  docker-compose logs -f
```

### Limpiar y compilar el proyecto
```bash
  mvn clean compile
```

### Ejecutar pruebas
```bash
  mvn test
```

### Empaquetar la aplicación
```bash
  mvn package -DskipTests
```

### Ejecutar la aplicación
```bash
  java -jar target/marketplace-0.0.1-SNAPSHOT.jar
```

### O con Maven directamente
```bash
  mvn spring-boot:run
```

### Health check
curl http://localhost:8080/actuator/health

#### Respuesta esperada:
#### {"status":"UP"}

### Crear un producto
curl -X POST http://localhost:8080/api/v1/products \
-H "Content-Type: application/json" \
-d '{
"name": "Laptop Gaming",
"description": "High performance gaming laptop",
"price": 1299.99,
"stock": 5,
"sellerId": 1
}'

### Con Maven
```bash
mvn spring-boot:run -Dspring.profiles.active=docker
```

### Con JAR
```bash
java -jar target/marketplace-0.0.1-SNAPSHOT.jar --spring.profiles.active=docker
```

# ⚙️ Configuración

## Perfiles de Configuración		

| Perfil     | Descripción                                     | Archivo                       |
|------------|-------------------------------------------------|-------------------------------|
| default    | Configuración local con bases de datos locales  | application.yaml              |
| docker     | Configuración para contenedores Docker          | application-docker.properties |

# 📡 Endpoints API

## Productos
### Crear Producto

```bash
  http
POST /api/v1/products
Content-Type: application/json
```
#### Request Body:
```bash
  json
{
    "name": "Laptop Gaming",
    "description": "High performance gaming laptop",
    "price": 1299.99,
    "stock": 5,
    "sellerId": 1
}
```
#### Response (201 Created):
```bash
  json
{
    "id": 1,
    "name": "Laptop Gaming",
    "description": "High performance gaming laptop",
    "price": 1299.99,
    "stock": 5,
    "sellerId": 1,
    "status": "ACTIVE",
    "createdAt": "2026-03-31T12:00:00",
    "updatedAt": "2026-03-31T12:00:00"
}
```
# 📁 Estructura del Proyecto

marketplace/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ConectaClick/marketplace/
│   │   │       ├── MarketplaceApplication.java
│   │   │       ├── domain/                    # Capa de Dominio
│   │   │       │   ├── model/                 # Entidades de negocio
│   │   │       │   │   ├── Product.java
│   │   │       │   │   └── User.java
│   │   │       │   ├── ports/                 # Puertos (interfaces)
│   │   │       │   │   ├── inbound/           # Casos de uso
│   │   │       │   │   │   └── CreateProductUseCase.java
│   │   │       │   │   └── outbound/          # Repositorios
│   │   │       │   │       ├── ProductRepositoryPort.java
│   │   │       │   │       └── UserRepositoryPort.java
│   │   │       │   └── exceptions/            # Excepciones de dominio
│   │   │       │       ├── ProductDomainException.java
│   │   │       │       └── UserNotFoundException.java
│   │   │       ├── application/               # Capa de Aplicación
│   │   │       │   └── services/              # Implementación de casos de uso
│   │   │       │       └── CreateProductService.java
│   │   │       └── infrastructure/            # Capa de Infraestructura
│   │   │           ├── persistence/           # Adaptadores JPA/PostgreSQL
│   │   │           │   ├── entities/          # Entidades JPA
│   │   │           │   │   ├── UserEntity.java
│   │   │           │   │   └── ProductEntity.java
│   │   │           │   ├── repositories/      # Repositorios Spring Data
│   │   │           │   │   ├── JpaUserRepository.java
│   │   │           │   │   └── JpaProductRepository.java
│   │   │           │   ├── adapters/          # Implementación de puertos
│   │   │           │   │   ├── ProductRepositoryAdapter.java
│   │   │           │   │   └── UserRepositoryAdapter.java
│   │   │           │   └── mappers/           # Mapeo entidades
│   │   │           │       ├── ProductPersistenceMapper.java
│   │   │           │       └── UserPersistenceMapper.java
│   │   │           ├── nosql/                 # Adaptadores MongoDB
│   │   │           │   ├── entities/          # Documentos MongoDB
│   │   │           │   │   └── ActivityLogEntity.java
│   │   │           │   ├── repositories/      # Repositorios MongoDB
│   │   │           │   │   └── ActivityLogRepository.java
│   │   │           │   └── services/          # Servicios NoSQL
│   │   │           │       └── LoggingService.java
│   │   │           ├── web/                   # Controladores REST
│   │   │           │   ├── controllers/       # Endpoints API
│   │   │           │   │   └── ProductController.java
│   │   │           │   ├── dto/               # Data Transfer Objects
│   │   │           │   │   ├── CreateProductRequest.java
│   │   │           │   │   └── ProductResponse.java
│   │   │           │   └── mappers/           # Mapeo DTOs
│   │   │           │       └── ProductRestMapper.java
│   │   │           └── security/              # Configuración Security
│   │   │               └── SecurityConfig.java
│   │   └── resources/
│   │       ├── application.properties         # Configuración por defecto
│   │       └── application-docker.properties  # Configuración para Docker
│   └── test/                                  # Pruebas unitarias e integración
│       └── java/
│           └── com/ConectaClick/marketplace/
│               └── application/services/
│                   └── CreateProductServiceTest.java
├── docker-compose.yml                         # Orquestación de servicios
├── Dockerfile                                  # Imagen de la aplicación
├── init-db.sql                                 # Script inicial PostgreSQL
├── init-mongo.js                               # Script inicial MongoDB
├── pom.xml                                     # Dependencias Maven
└── README.md                                   # Este archivo

# 🧪 Pruebas
## Ejecutar todas las pruebas
```bash
  mvn test
```
## Generar reporte de cobertura
```bash
  mvn jacoco:report
```

# 🐳 Despliegue con Docker
## Comandos básicos
```bash
  # Construir la imagen de la aplicación
docker build -t marketplace-app:latest .

# Levantar todos los servicios
docker-compose up -d

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f app

# Ver estado de los servicios
docker-compose ps

# Detener servicios (conserva datos)
docker-compose down

# Detener y eliminar volúmenes (borra todos los datos)
docker-compose down -v

# Reiniciar un servicio
docker-compose restart app
```

# 🔍 Solución de Problemas
## Problema: Spring Security está protegiendo los endpoints.

## Solución: Deshabilitar seguridad temporalmente en application.yaml:

### properties
### spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

## Error: MongoDB connection refused
## Problema: MongoDB no está corriendo.

## Solución:

```bash
  # Verificar estado
docker ps | grep mongodb

# Ver logs
docker logs marketplace_mongodb

# Reiniciar servicio
docker-compose restart mongodb
```
## Error: PostgreSQL connection refused
## Problema: PostgreSQL no está corriendo.

## Solución:

```bash
# Verificar estado
docker ps | grep postgres

# Ver logs
docker logs marketplace_postgres

# Reiniciar servicio
docker-compose restart postgres
```
## Error: Port already in use
## Problema: El puerto 8080, 5432 o 27017 está ocupado.

## Solución: Cambiar puertos en docker-compose.yml:

```bash
  yaml
  ports:
- "8081:8080"   # API en puerto 8081
- "5433:5432"   # PostgreSQL en puerto 5433
- "27018:27017" # MongoDB en puerto 27018
```
## Error: MalformedInputException en archivos .properties
## Problema: Archivo de propiedades con codificación incorrecta.

## Solución: Guardar los archivos con codificación UTF-8 sin BOM.

## Error: Java version not supported
## Problema: Versión de Java incorrecta.

## Solución: Verificar la versión de Java:

```bash
  java -version
# Debe mostrar: openjdk version "25"
```
## Error: OutOfMemoryError
## Problema: Memoria insuficiente para Docker.

## Solución: Aumentar memoria en Docker Desktop:

### ° Windows/Mac: Docker Desktop → Settings → Resources → Memory
### ° Linux: Ajustar en Docker daemon}

# 👥 Autores
## Marlon Valbuena - Desarrollador Principal - @Programvr