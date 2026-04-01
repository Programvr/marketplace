package com.ConectaClick.marketplace.infrastructure.persistence.repositories;

import com.ConectaClick.marketplace.infrastructure.persistence.entities.UserEntity;
import com.ConectaClick.marketplace.infrastructure.persistence.entities.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndUserType(Long id, UserType userType);
    boolean existsByIdAndUserType(Long id, UserType userType);
}