package src.lab.db.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import src.lab.db.models.Link;
import src.lab.db.models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LinkRepositoryTest {

    @Autowired
    private LinksRepository linkRepository;

    @Autowired
    private UsersRepository usersRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user1");
        testUser = usersRepository.save(testUser);
    }

    @Test
    void findByShortCode_existingCode_returnsLink() {
        Link link = createLink("abc123", "https://example.com");

        Optional<Link> result = linkRepository.findByShortCode("abc123");

        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getShortCode());
    }

    @Test
    void findByShortCode_nonExistingCode_returnsEmpty() {
        Optional<Link> result = linkRepository.findByShortCode("xyz");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUserId_returnsUserLinks() {
        createLink("abc1", "https://example.com/1");
        createLink("abc2", "https://example.com/2");

        List<Link> results = linkRepository.findByUserId("user1");

        assertEquals(2, results.size());
    }

    @Test
    void findByUserId_differentUser_returnsEmpty() {
        createLink("abc1", "https://example.com/1");

        List<Link> results = linkRepository.findByUserId("user2");

        assertTrue(results.isEmpty());
    }

    @Test
    void findByExpiresAtBefore_returnsExpiredLinks() {
        Link expiredLink = createLink("exp1", "https://example.com/exp");
        expiredLink.setExpiresAt(LocalDateTime.now().minusHours(1));
        linkRepository.save(expiredLink);

        Link activeLink = createLink("act1", "https://example.com/act");
        activeLink.setExpiresAt(LocalDateTime.now().plusHours(1));
        linkRepository.save(activeLink);

        List<Link> expired = linkRepository.findByExpiresAtBefore(LocalDateTime.now());

        assertEquals(1, expired.size());
        assertEquals("exp1", expired.get(0).getShortCode());
    }

    private Link createLink(String shortCode, String url) {
        Link link = new Link();
        link.setShortCode(shortCode);
        link.setOriginalUrl(url);
        link.setUser(testUser);
        link.setClickLimit(10);
        link.setExpiresAt(LocalDateTime.now().plusHours(24));
        return linkRepository.save(link);
    }
}
