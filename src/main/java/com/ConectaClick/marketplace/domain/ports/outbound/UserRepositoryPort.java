package com.conectaclick.marketplace.domain.ports.outbound;

import com.conectaclick.marketplace.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    boolean existsByIdAndType(Long id, User.UserType userType);
}