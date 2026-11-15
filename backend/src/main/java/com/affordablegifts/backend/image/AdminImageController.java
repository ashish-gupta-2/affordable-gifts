package com.affordablegifts.backend.image;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminImageController {

    private final ImageService service;

    public AdminImageController(ImageService service) {
        this.service = service;
    }

    /** The request body is the raw image file (no multipart). */
    @PostMapping("/api/admin/images")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(HttpServletRequest request) throws IOException {
        // Read one byte past the limit so an oversized or chunked upload is caught
        // without buffering all of it.
        byte[] data = request.getInputStream().readNBytes(ImageService.MAX_BYTES + 1);
        return Map.of("url", service.store(data));
    }
}
