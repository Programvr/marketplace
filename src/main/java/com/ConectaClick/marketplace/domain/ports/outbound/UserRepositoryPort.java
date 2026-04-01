package com.ConectaClick.marketplace.domain.ports.outbound;

import com.ConectaClick.marketplace.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    boolean existsByIdAndType(Long id, User.UserType userType);
}