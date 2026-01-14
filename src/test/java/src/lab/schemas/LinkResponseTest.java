package src.lab.schemas;

import org.junit.jupiter.api.Test;
import src.lab.db.models.Link;
import src.lab.db.models.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LinkResponseTest {

    @Test
    void from_convertsShortLinkToResponse() {
        User user = new User();
        user.setId("user1");

        Link link = new Link();
        link.setShortCode("abc123");
        link.setOriginalUrl("https://example.com");
        link.setClickLimit(10);
        link.setClickCount(5);
        link.setCreatedAt(LocalDateTime.now());
        link.setExpiresAt(LocalDateTime.now().plusHours(24));
        link.setUser(user);

        LinkResponse response = LinkResponse.from(link);

        assertNotNull(response);
        assertEquals("abc123", response.getShortCode());
        assertEquals("https://example.com", response.getOriginalUrl());
        assertEquals(10, response.getClickLimit());
        assertEquals(5, response.getClickCount());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getExpiresAt());
        assertTrue(response.isActive());
    }

    @Test
    void from_inactiveLink_setsActiveFalse() {
        User user = new User();
        user.setId("user1");

        Link link = new Link();
        link.setShortCode("abc123");
        link.setOriginalUrl("https://example.com");
        link.setClickLimit(10);
        link.setClickCount(10);
        link.setCreatedAt(LocalDateTime.now());
        link.setExpiresAt(LocalDateTime.now().plusHours(24));
        link.setUser(user);

        LinkResponse response = LinkResponse.from(link);

        assertFalse(response.isActive());
    }
}
