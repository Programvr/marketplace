package com.conectaclick.marketplace.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private String name;
    private UserType userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum UserType {
        SELLER, BUYER
    }

    public boolean isSeller() {
        return this.userType == UserType.SELLER;
    }
}
