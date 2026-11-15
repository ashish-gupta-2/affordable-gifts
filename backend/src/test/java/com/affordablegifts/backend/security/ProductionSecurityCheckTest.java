package com.affordablegifts.backend.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductionSecurityCheckTest {

    @Test
    void refusesWeakAdminKey() {
        assertThrows(IllegalStateException.class, () -> new ProductionSecurityCheck("local-test-key").check());
        assertThrows(IllegalStateException.class, () -> new ProductionSecurityCheck("").check());
    }

    @Test
    void acceptsLongAdminKey() {
        assertDoesNotThrow(() -> new ProductionSecurityCheck("a".repeat(32)).check());
    }
}
