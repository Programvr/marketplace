package com.conectaclick.marketplace.infrastructure.persistence.adapters;

import com.conectaclick.marketplace.domain.model.User;
import com.conectaclick.marketplace.domain.ports.outbound.UserRepositoryPort;
import com.conectaclick.marketplace.infrastructure.persistence.entities.enums.UserType;
import com.conectaclick.marketplace.infrastructure.persistence.mappers.UserPersistenceMapper;
import com.conectaclick.marketplace.infrastructure.persistence.repositories.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByIdAndType(Long id, User.UserType userType) {
        UserType jpaUserType = UserType.valueOf(userType.name());
        return jpaUserRepository.existsByIdAndUserType(id, jpaUserType);
    }
}
