package src.lab.db.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    private Link link;

    @BeforeEach
    void setUp() {
        link = new Link();
        link.setExpiresAt(LocalDateTime.now().plusHours(24));
        link.setClickLimit(10);
        link.setClickCount(0);
    }

    @Test
    void isExpired_futureExpiry_returnsFalse() {
        link.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertFalse(link.isExpired());
    }

    @Test
    void isExpired_pastExpiry_returnsTrue() {
        link.setExpiresAt(LocalDateTime.now().minusHours(1));

        assertTrue(link.isExpired());
    }

    @Test
    void isLimitReached_belowLimit_returnsFalse() {
        link.setClickLimit(10);
        link.setClickCount(5);

        assertFalse(link.isLimitReached());
    }

    @Test
    void isLimitReached_atLimit_returnsTrue() {
        link.setClickLimit(10);
        link.setClickCount(10);

        assertTrue(link.isLimitReached());
    }

    @Test
    void isLimitReached_aboveLimit_returnsTrue() {
        link.setClickLimit(10);
        link.setClickCount(15);

        assertTrue(link.isLimitReached());
    }

    @Test
    void isActive_notExpiredNotLimitReached_returnsTrue() {
        link.setExpiresAt(LocalDateTime.now().plusHours(1));
        link.setClickLimit(10);
        link.setClickCount(5);

        assertTrue(link.isActive());
    }

    @Test
    void isActive_expired_returnsFalse() {
        link.setExpiresAt(LocalDateTime.now().minusHours(1));
        link.setClickLimit(10);
        link.setClickCount(5);

        assertFalse(link.isActive());
    }

    @Test
    void isActive_limitReached_returnsFalse() {
        link.setExpiresAt(LocalDateTime.now().plusHours(1));
        link.setClickLimit(10);
        link.setClickCount(10);

        assertFalse(link.isActive());
    }

    @Test
    void onCreate_setsCreatedAt() {
        link.onCreate();

        assertNotNull(link.getCreatedAt());
        assertTrue(link.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
