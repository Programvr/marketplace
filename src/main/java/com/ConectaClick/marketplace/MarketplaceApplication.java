package com.conectaclick.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.ConectaClick.marketplace.infrastructure.persistence.repositories"
})
@EntityScan(basePackages = {
        "com.ConectaClick.marketplace.infrastructure.persistence.entities"
})
@EnableMongoRepositories(basePackages = {
        "com.ConectaClick.marketplace.infrastructure.nosql.repositories"
})
public class MarketplaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketplaceApplication.class, args);
	}

}
