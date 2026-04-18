package com.conectaclick.marketplace.infrastructure.persistence.mappers;

import com.conectaclick.marketplace.domain.model.User;
import com.conectaclick.marketplace.infrastructure.persistence.entities.UserEntity;
import com.conectaclick.marketplace.infrastructure.persistence.entities.enums.UserType;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .userType(User.UserType.valueOf(entity.getUserType().name()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setName(user.getName());
        entity.setUserType(UserType.valueOf(user.getUserType().name()));
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}