package com.affordablegifts.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Own Spring context with tiny limits so the lockouts can be reached quickly. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.orders-per-hour=3",
        "app.security.admin-failures-per-15-min=3"})
class RateLimitTest {

    @Autowired
    MockMvc mvc;

    @Test
    void ordersAreCappedPerClient() throws Exception {
        for (int i = 0; i < 3; i++) {
            int code = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andReturn().getResponse().getStatus();
            assertEquals(400, code);
        }
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void repeatedWrongAdminKeysLockTheClientOut() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/api/admin/orders").header("X-Admin-Key", "guess" + i))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/admin/orders").header("X-Admin-Key", "guess-again"))
                .andExpect(status().isTooManyRequests());
        // Even the right key is refused while locked out.
        mvc.perform(get("/api/admin/orders").header("X-Admin-Key", "test-admin-key"))
                .andExpect(status().isTooManyRequests());
    }
}
