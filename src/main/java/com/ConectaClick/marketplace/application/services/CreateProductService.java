package com.ConectaClick.marketplace.application.services;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.model.User;
import com.ConectaClick.marketplace.domain.exceptions.UserNotFoundException;
import com.ConectaClick.marketplace.domain.ports.inbound.CreateProductUseCase;
import com.ConectaClick.marketplace.domain.ports.outbound.ProductRepositoryPort;
import com.ConectaClick.marketplace.domain.ports.outbound.UserRepositoryPort;
import com.ConectaClick.marketplace.infrastructure.events.ProductCreatedEvent;
import com.ConectaClick.marketplace.infrastructure.nosql.services.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio de aplicación que implementa el caso de uso: Crear Producto
 *
 * Este servicio sigue los principios de Arquitectura Hexagonal:
 * - Depende de puertos (interfaces) no de implementaciones concretas
 * - Contiene la lógica de negocio del caso de uso
 * - Es agnóstico de la infraestructura (base de datos, web, etc.)
 *
 * @author ConectaClick Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateProductService implements CreateProductUseCase {

    // Puertos (dependencias abstraídas)
    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    // Infraestructura adicional (opcional pero recomendada)
    private final LoggingService loggingService;
    private final ApplicationEventPublisher eventPublisher;

    // Constantes de negocio
    private static final BigDecimal MINIMUM_PRICE = new BigDecimal("0.01");
    private static final int MAX_NAME_LENGTH = 200;
    private static final String PRODUCT_CREATED_ACTION = "PRODUCT_CREATED";
    private static final String PRODUCT_ENTITY_TYPE = "PRODUCT";
    private static final String LOG_LEVEL_INFO = "INFO";
    private static final String LOG_LEVEL_WARNING = "WARNING";

    /**
     * Método principal que ejecuta el caso de uso completo
     *
     * @param command Comando con los datos del producto a crear
     * @return Producto creado con su ID asignado
     * @throws IllegalArgumentException Si los datos de entrada son inválidos
     * @throws UserNotFoundException Si el vendedor no existe
     */
    @Override
    public Product execute(CreateProductCommand command) {
        // Inicio del proceso con logging estructurado
        log.info(" Iniciando creación de producto - Seller ID: {}, Product: {}",
                command.sellerId(), command.name());

        long startTime = System.currentTimeMillis();

        try {
            // 1. Validación exhaustiva de datos de entrada
            validateCommand(command);
            log.debug(" Validación de datos exitosa");

            // 2. Verificar existencia y tipo de usuario vendedor
            User seller = validateAndGetSeller(command.sellerId());
            log.debug(" Vendedor validado - ID: {}, Nombre: {}, Email: {}",
                    seller.getId(), seller.getName(), seller.getEmail());

            // 3. Crear entidad de dominio Product con reglas de negocio
            Product newProduct = createProductEntity(command);
            log.debug(" Entidad Product creada en dominio - Nombre: {}, Precio: {}, Stock: {}",
                    newProduct.getName(), newProduct.getPrice(), newProduct.getStock());

            // 4. Persistir en PostgreSQL (base de datos transaccional)
            Product savedProduct = productRepositoryPort.save(newProduct);
            log.info(" Producto persistido en PostgreSQL - ID asignado: {}", savedProduct.getId());

            // 5. Guardar log en MongoDB
            saveActivityLog(savedProduct, seller);

            // 6. Publicar evento para otros microservicios (Spring Events)
            publishProductCreatedEvent(savedProduct, seller);

            // 7. Registrar métricas y tiempo de ejecución
            long executionTime = System.currentTimeMillis() - startTime;
            log.info(" Producto creado exitosamente en {} ms - ID: {}, Nombre: {}",
                    executionTime, savedProduct.getId(), savedProduct.getName());

            // 8. Retornar producto para el controlador REST
            return savedProduct;

        } catch (IllegalArgumentException | UserNotFoundException e) {
            // Log de errores de negocio
            log.error(" Error de negocio al crear producto - Command: {}, Error: {}",
                    command, e.getMessage());

            // Guardar log de error en MongoDB
            loggingService.logActivity(
                    command.sellerId(),
                    "PRODUCT_CREATION_FAILED",
                    "PRODUCT",
                    null,
                    String.format("Error: %s - Command: %s", e.getMessage(), command),
                    LOG_LEVEL_WARNING
            );

            throw e;
        } catch (Exception e) {
            // Log de errores técnicos no esperados
            log.error(" Error técnico al crear producto - Command: {}, Exception: {}",
                    command, e.getMessage());

            // Guardar log de error crítico en MongoDB
            loggingService.logActivity(
                    command.sellerId(),
                    "PRODUCT_CREATION_ERROR",
                    "PRODUCT",
                    null,
                    String.format("Technical error: %s", e.getMessage()),
                    "ERROR"
            );

            throw new RuntimeException("Error técnico al crear el producto. Por favor, intente nuevamente.", e);
        }
    }

    /**
     * Valida exhaustivamente todos los campos del comando
     *
     * @param command Comando a validar
     * @throws IllegalArgumentException Si algún campo no cumple las reglas
     */
    private void validateCommand(CreateProductCommand command) {
        // Validación de nombre
        if (command.name() == null || command.name().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (command.name().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("El nombre del producto no puede exceder los %d caracteres", MAX_NAME_LENGTH)
            );
        }

        // Validación de precio
        if (command.price() == null) {
            throw new IllegalArgumentException("El precio del producto es obligatorio");
        }

        if (command.price().compareTo(MINIMUM_PRICE) < 0) {
            throw new IllegalArgumentException(
                    String.format("El precio debe ser mayor a %.2f", MINIMUM_PRICE)
            );
        }

        if (command.price().scale() > 2) {
            throw new IllegalArgumentException("El precio no puede tener más de 2 decimales");
        }

        // Validación de stock
        if (command.stock() == null) {
            throw new IllegalArgumentException("El stock del producto es obligatorio");
        }

        if (command.stock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        // Validación de vendedor
        if (command.sellerId() == null || command.sellerId() <= 0) {
            throw new IllegalArgumentException("ID de vendedor inválido");
        }

        log.debug("Validación completada exitosamente para producto: {}", command.name());
    }

    /**
     * Valida que el vendedor existe y sea de tipo SELLER
     *
     * @param sellerId ID del vendedor a validar
     * @return User objeto del vendedor
     * @throws UserNotFoundException Si el usuario no existe
     * @throws IllegalArgumentException Si el usuario no es vendedor
     */
    private User validateAndGetSeller(Long sellerId) {
        User seller = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> {
                    log.warn("Vendedor no encontrado - ID: {}", sellerId);
                    return new UserNotFoundException(sellerId);
                });

        if (!seller.isSeller()) {
            log.warn("Usuario no es vendedor - ID: {}, Tipo: {}", sellerId, seller.getUserType());
            throw new IllegalArgumentException(
                    String.format("El usuario con ID %d no tiene permisos de vendedor", sellerId)
            );
        }

        return seller;
    }

    /**
     * Crea la entidad de dominio Product aplicando las reglas de negocio
     *
     * @param command Comando con los datos del producto
     * @return Entidad Product del dominio
     */
    private Product createProductEntity(CreateProductCommand command) {
        // Aplicar reglas de negocio adicionales
        String sanitizedName = sanitizeProductName(command.name());
        String sanitizedDescription = sanitizeDescription(command.description());

        // Validar reglas de negocio específicas del dominio
        validateBusinessRules(command.price(), command.stock());

        // Crear producto usando el factory method del dominio
        return Product.createNew(
                sanitizedName,
                sanitizedDescription,
                command.price(),
                command.stock(),
                command.sellerId()
        );
    }

    /**
     * Sanitiza el nombre del producto (elimina espacios extras, caracteres especiales)
     */
    private String sanitizeProductName(String name) {
        if (name == null) return null;
        // Eliminar espacios al inicio y final, reducir múltiples espacios a uno
        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Sanitiza la descripción del producto
     */
    private String sanitizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return description.trim();
    }

    /**
     * Valida reglas de negocio específicas
     */
    private void validateBusinessRules(BigDecimal price, Integer stock) {
        // Regla: El precio no puede ser mayor a 1,000,000 (productos de lujo requieren aprobación)
        BigDecimal MAX_PRICE_NO_APPROVAL = new BigDecimal("1000000");
        if (price.compareTo(MAX_PRICE_NO_APPROVAL) > 0) {
            log.info("Producto de alto valor detectado: {} - Requiere aprobación especial", price);
            // Aquí se podría lanzar una excepción o marcar para aprobación
            // Por ahora solo logueamos
        }

        // Regla: Si el stock es 0, el producto se crea como "IN_STOCK"
        if (stock == 0) {
            log.info("Producto creado con stock inicial cero - Marcado como sin stock");
        }
    }

    /**
     * Guarda el log de auditoría en MongoDB
     */
    private void saveActivityLog(Product savedProduct, User seller) {
        try {
            String logDetails = String.format(
                    "Producto creado - Nombre: %s, Precio: %s, Stock: %d, Vendedor: %s (ID: %d)",
                    savedProduct.getName(),
                    savedProduct.getPrice(),
                    savedProduct.getStock(),
                    seller.getName(),
                    seller.getId()
            );

            loggingService.logActivity(
                    savedProduct.getSellerId(),
                    PRODUCT_CREATED_ACTION,
                    PRODUCT_ENTITY_TYPE,
                    savedProduct.getId(),
                    logDetails,
                    LOG_LEVEL_INFO
            );

            log.debug("Log de auditoría guardado en MongoDB - Product ID: {}", savedProduct.getId());
        } catch (Exception e) {
            // No falla la creación del producto si falla el log
            log.error("Error al guardar log en MongoDB - Product ID: {}, Error: {}",
                    savedProduct.getId(), e.getMessage());
        }
    }

    /**
     * Publica evento de dominio para otros microservicios
     */
    private void publishProductCreatedEvent(Product savedProduct, User seller) {
        try {
            ProductCreatedEvent event = ProductCreatedEvent.builder()
                    .productId(savedProduct.getId())
                    .productName(savedProduct.getName())
                    .price(savedProduct.getPrice())
                    .stock(savedProduct.getStock())
                    .sellerId(savedProduct.getSellerId())
                    .sellerName(seller.getName())
                    .sellerEmail(seller.getEmail())
                    .createdAt(LocalDateTime.now())
                    .eventType("PRODUCT_CREATED")
                    .source("marketplace-api")
                    .build();

            eventPublisher.publishEvent(event);

            log.info("Evento ProductCreated publicado - Product ID: {}, Event: {}",
                    savedProduct.getId(), event);
        } catch (Exception e) {
            // No falla la creación del producto si falla el evento
            log.error("Error al publicar evento ProductCreated - Product ID: {}, Error: {}",
                    savedProduct.getId(), e.getMessage());
        }
    }
}