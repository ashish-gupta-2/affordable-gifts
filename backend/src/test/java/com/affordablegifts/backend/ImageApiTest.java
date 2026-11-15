package com.affordablegifts.backend;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImageApiTest {

    private static final String KEY = "X-Admin-Key";
    private static final String GOOD_KEY = "test-admin-key";

    // Smallest valid-looking files: only the leading "magic bytes" matter to the check.
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V'};

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.update("delete from product_packs");
        jdbc.update("delete from products");
        jdbc.update("delete from packaging_options");
        jdbc.update("delete from images");
    }

    private String upload(byte[] bytes) throws Exception {
        String body = mvc.perform(post("/api/admin/images").header(KEY, GOOD_KEY)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(bytes))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void uploadedImageIsServedPubliclyWithItsRealTypeAndLongCache() throws Exception {
        String url = upload(PNG);
        byte[] served = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Cache-Control", startsWith("max-age=31536000")))
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(PNG, served);
    }

    @Test
    void acceptsJpegAndWebp() throws Exception {
        mvc.perform(get(upload(JPEG))).andExpect(content().contentType("image/jpeg"));
        mvc.perform(get(upload(WEBP))).andExpect(content().contentType("image/webp"));
    }

    @Test
    void uploadNeedsTheAdminKey() throws Exception {
        mvc.perform(post("/api/admin/images").contentType(MediaType.APPLICATION_OCTET_STREAM).content(PNG))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refusesFilesThatAreNotJpegPngOrWebp() throws Exception {
        // SVG can carry scripts, and a renamed text file is not an image.
        for (String fake : new String[] {"<svg xmlns='http://www.w3.org/2000/svg'><script>alert(1)</script></svg>",
                "just some text, not an image", "GIF89a......"}) {
            mvc.perform(post("/api/admin/images").header(KEY, GOOD_KEY)
                            .contentType("image/png").content(fake.getBytes()))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/admin/images").header(KEY, GOOD_KEY)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(new byte[0]))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refusesImagesOverOneMegabyte() throws Exception {
        byte[] big = new byte[1_000_001];
        System.arraycopy(PNG, 0, big, 0, PNG.length);
        mvc.perform(post("/api/admin/images").header(KEY, GOOD_KEY)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(big))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownImageIs404() throws Exception {
        mvc.perform(get("/api/images/00000000-0000-0000-0000-000000000000")).andExpect(status().isNotFound());
        mvc.perform(get("/api/images/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void productAndPackagingKeepTheirPhotoAndStorefrontShowsIt() throws Exception {
        String url = upload(PNG);

        mvc.perform(post("/api/admin/products").header(KEY, GOOD_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"walnuts","name":"Walnuts","category":"Dry Fruits","imageUrl":"%s",
                                 "packs":[{"weight":100,"price":150}]}""".formatted(url)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").value(url))
                .andExpect(jsonPath("$.emoji").value(""));

        mvc.perform(post("/api/admin/packaging").header(KEY, GOOD_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"box-9","name":"Box","kind":"box","description":"d","price":30,
                                 "slotCount":2,"slotCapacity":100,"imageUrl":"%s"}""".formatted(url)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/products")).andExpect(jsonPath("$[0].imageUrl").value(url));
        mvc.perform(get("/api/packaging")).andExpect(jsonPath("$[0].imageUrl").value(url));

        // Removing the photo again (null) works on update.
        mvc.perform(put("/api/admin/products/walnuts").header(KEY, GOOD_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Walnuts","category":"Dry Fruits","emoji":"W","active":true,
                                 "packs":[{"weight":100,"price":150}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").doesNotExist());
    }

    @Test
    void imageUrlMustBeOneOfOurOwnUploads() throws Exception {
        // Otherwise an admin typo (or a stolen key) could point customers at any website.
        for (String bad : new String[] {"https://evil.example/x.png", "javascript:alert(1)",
                "/api/images/../admin/orders", "/api/images/not-a-uuid"}) {
            mvc.perform(post("/api/admin/products").header(KEY, GOOD_KEY).contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"id":"w","name":"W","category":"Snacks","emoji":"w","imageUrl":"%s",
                                     "packs":[{"weight":100,"price":1}]}""".formatted(bad)))
                    .andExpect(status().isBadRequest());
        }
    }
}
