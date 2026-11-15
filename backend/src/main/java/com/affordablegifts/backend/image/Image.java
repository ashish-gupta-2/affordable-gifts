package com.affordablegifts.backend.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "images")
public class Image {

    @Id
    private UUID id;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private byte[] data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Image() {
    }

    public Image(UUID id, String contentType, byte[] data) {
        this.id = id;
        this.contentType = contentType;
        this.data = data;
    }

    public UUID getId() { return id; }
    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }
}
