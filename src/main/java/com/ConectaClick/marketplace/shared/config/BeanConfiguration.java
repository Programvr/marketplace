package com.ConectaClick.marketplace.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.marketplace")
@EnableJpaRepositories(basePackages = "com.marketplace.infrastructure.persistence.repositories")
public class BeanConfiguration {
    // Configuración adicional de beans si es necesario
}
