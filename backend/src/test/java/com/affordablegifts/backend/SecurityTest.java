package com.affordablegifts.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void everyResponseCarriesSecurityHeaders() throws Exception {
        mvc.perform(get("/api/settings"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'none'; frame-ancestors 'none'"));
    }

    @Test
    void orderAndAdminResponsesAreNeverCached() throws Exception {
        mvc.perform(get("/api/admin/orders").header("X-Admin-Key", "nope"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void guessableRequestIdIsRejected() throws Exception {
        String body = """
                {"requestId":"weak1","packagingId":"box-2","quantity":1,"productIds":["a","b"],
                 "customer":{"name":"A","phone":"9876543210","email":"a@b.co"},
                 "address":{"house":"1","street":"s","area":"a","city":"c","state":"s","pincode":"560001"}}""";
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedBodyIsRefused() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("x".repeat(25_000)))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void errorBodiesDoNotRevealInternals() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").doesNotExist())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void adminTricksNeverReturnData() throws Exception {
        for (String path : new String[] {"//api/admin/orders", "/api/admin/orders;x=1",
                "/api/%61dmin/orders", "/api/admin/../admin/orders", "/api/admin/orders/"}) {
            int code = mvc.perform(get(path)).andReturn().getResponse().getStatus();
            org.junit.jupiter.api.Assertions.assertNotEquals(200, code, path);
        }
    }

    @Test
    void unknownPathsRevealOnlyTheStatus() throws Exception {
        mvc.perform(get("/api/nope")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }
}
