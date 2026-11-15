package com.affordablegifts.backend.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In production the app refuses to start with a weak admin key. */
@Component
@Profile("prod")
public class ProductionSecurityCheck {

    static final int MIN_ADMIN_KEY_LENGTH = 24;

    private final String adminKey;

    public ProductionSecurityCheck(@Value("${app.admin.api-key:}") String adminKey) {
        this.adminKey = adminKey;
    }

    @PostConstruct
    void check() {
        if (adminKey.length() < MIN_ADMIN_KEY_LENGTH) {
            throw new IllegalStateException("ADMIN_API_KEY must be at least " + MIN_ADMIN_KEY_LENGTH
                    + " characters in production. Generate one with: openssl rand -hex 32");
        }
    }
}
