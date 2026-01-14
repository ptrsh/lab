package src.lab.schemas;

import lombok.Data;
import src.lab.db.models.Link;

import java.time.LocalDateTime;

@Data
public class LinkResponse {
    private String shortCode;
    private String originalUrl;
    private int clickLimit;
    private int clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;

    public static LinkResponse from(Link link) {
        LinkResponse response = new LinkResponse();
        response.setShortCode(link.getShortCode());
        response.setOriginalUrl(link.getOriginalUrl());
        response.setClickLimit(link.getClickLimit());
        response.setClickCount(link.getClickCount());
        response.setCreatedAt(link.getCreatedAt());
        response.setExpiresAt(link.getExpiresAt());
        response.setActive(link.isActive());
        return response;
    }
}
