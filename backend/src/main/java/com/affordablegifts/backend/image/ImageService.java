package com.affordablegifts.backend.image;

import com.affordablegifts.backend.common.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores uploaded photos. This is the only class that knows where the bytes live, so
 * moving to object storage (R2 / S3) later means changing it and nothing else.
 */
@Service
public class ImageService {

    public static final int MAX_BYTES = 1_000_000;
    public static final String URL_PREFIX = "/api/images/";
    /** What a catalogue row may hold in its imageUrl field: one of our own uploads. */
    public static final String URL_REGEX =
            "/api/images/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private final ImageRepository repository;

    public ImageService(ImageRepository repository) {
        this.repository = repository;
    }

    /** Saves the image and returns the URL the storefront should use. */
    @Transactional
    public String store(byte[] data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("No image was uploaded");
        }
        if (data.length > MAX_BYTES) {
            throw new IllegalArgumentException("Image must be 1 MB or smaller");
        }
        // Trust the bytes, not the Content-Type header. SVG is refused on purpose:
        // it can carry scripts.
        String type = detectType(data);
        if (type == null) {
            throw new IllegalArgumentException("Only JPEG, PNG or WebP images are allowed");
        }
        Image image = repository.save(new Image(UUID.randomUUID(), type, data));
        return URL_PREFIX + image.getId();
    }

    @Transactional(readOnly = true)
    public Image get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));
    }

    private static String detectType(byte[] d) {
        if (d.length > 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length > 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G'
                && d[4] == 0x0D && d[5] == 0x0A && d[6] == 0x1A && d[7] == 0x0A) {
            return "image/png";
        }
        if (d.length > 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
