package com.example.movieticket.support.factory;

import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.Role;

public final class UserFactory {

    public static final String DEFAULT_PASSWORD_HASH = "hashed";

    private UserFactory() {
    }

    public static User customer(String email) {
        return new User(email, DEFAULT_PASSWORD_HASH, Role.CUSTOMER);
    }

    public static User admin(String email) {
        return new User(email, DEFAULT_PASSWORD_HASH, Role.ADMIN);
    }

    public static User withId(Long id, User user) {
        user.setId(id);
        return user;
    }
}
