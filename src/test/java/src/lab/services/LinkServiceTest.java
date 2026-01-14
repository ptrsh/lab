package src.lab.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import src.lab.db.models.Link;
import src.lab.db.models.User;
import src.lab.db.repositories.LinksRepository;
import src.lab.exceptions.*;
import src.lab.infra.settings.AppConfig;
import src.lab.services.impl.LinkServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinksRepository linkRepository;

    @Mock
    private UserService userService;

    @Mock
    private AppConfig config;

    @InjectMocks
    private LinkServiceImpl linkService;

    private User testUser;
    private Link testLink;
    private AppConfig.LinkConfig linkConfig;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user1");

        testLink = new Link();
        testLink.setId(1L);
        testLink.setShortCode("abc123");
        testLink.setOriginalUrl("https://example.com");
        testLink.setUser(testUser);
        testLink.setClickLimit(10);
        testLink.setClickCount(0);
        testLink.setCreatedAt(LocalDateTime.now());
        testLink.setExpiresAt(LocalDateTime.now().plusHours(24));

        linkConfig = new AppConfig.LinkConfig();
        linkConfig.setTtlHours(24);
        linkConfig.setDefaultClickLimit(100);
        linkConfig.setShortCodeLength(6);
    }

    private void setupConfig() {
        when(config.getLink()).thenReturn(linkConfig);
    }

    @Test
    void createShortLink_validUrl_createsLink() {
        setupConfig();
        when(userService.getOrCreateUser("user1")).thenReturn(testUser);
        when(linkRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(linkRepository.save(any(Link.class))).thenReturn(testLink);

        Link result = linkService.createShortLink("user1", "https://example.com", 10);

        assertNotNull(result);
        verify(userService).getOrCreateUser("user1");
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void createShortLink_withNullClickLimit_usesDefault() {
        setupConfig();
        when(userService.getOrCreateUser("user1")).thenReturn(testUser);
        when(linkRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> {
            Link link = invocation.getArgument(0);
            assertEquals(100, link.getClickLimit());
            return link;
        });

        linkService.createShortLink("user1", "https://example.com", null);

        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void createShortLink_invalidUrl_throwsException() {
        assertThrows(InvalidUrlException.class, () ->
                linkService.createShortLink("user1", "not-a-valid-url", 10));
    }

    @Test
    void createShortLink_uniqueCodeCollision_retriesAndSucceeds() {
        setupConfig();
        when(userService.getOrCreateUser("user1")).thenReturn(testUser);
        when(linkRepository.findByShortCode(anyString()))
                .thenReturn(Optional.of(testLink))
                .thenReturn(Optional.empty());
        when(linkRepository.save(any(Link.class))).thenReturn(testLink);

        Link result = linkService.createShortLink("user1", "https://example.com", 10);

        assertNotNull(result);
        verify(linkRepository, atLeast(2)).findByShortCode(anyString());
    }

    @Test
    void createShortLink_tooManyCollisions_throwsException() {
        setupConfig();
        when(userService.getOrCreateUser("user1")).thenReturn(testUser);
        when(linkRepository.findByShortCode(anyString())).thenReturn(Optional.of(testLink));

        assertThrows(ShortCodeGenerationException.class, () ->
                linkService.createShortLink("user1", "https://example.com", 10));
    }

    @Test
    void redirect_validLink_returnsUrlAndIncrementsCount() {
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));
        when(linkRepository.save(any(Link.class))).thenReturn(testLink);

        String url = linkService.redirect("abc123");

        assertEquals("https://example.com", url);
        verify(linkRepository).save(testLink);
        assertEquals(1, testLink.getClickCount());
    }

    @Test
    void redirect_linkNotFound_throwsException() {
        when(linkRepository.findByShortCode("xyz")).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () ->
                linkService.redirect("xyz"));
    }

    @Test
    void redirect_expiredLink_throwsException() {
        testLink.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));

        assertThrows(LinkExpiredException.class, () ->
                linkService.redirect("abc123"));
    }

    @Test
    void redirect_limitReached_throwsException() {
        testLink.setClickLimit(5);
        testLink.setClickCount(5);
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));

        assertThrows(LinkLimitReachedException.class, () ->
                linkService.redirect("abc123"));
    }

    @Test
    void getUserLinks_returnsUserLinks() {
        List<Link> links = Arrays.asList(testLink);
        when(linkRepository.findByUserId("user1")).thenReturn(links);

        List<Link> result = linkService.getUserLinks("user1");

        assertEquals(1, result.size());
        verify(linkRepository).findByUserId("user1");
    }

    @Test
    void getLink_validShortCode_returnsLink() {
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));

        Link result = linkService.getLink("abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getShortCode());
    }

    @Test
    void getLink_notFound_throwsException() {
        when(linkRepository.findByShortCode("xyz")).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () ->
                linkService.getLink("xyz"));
    }

    @Test
    void updateLink_validOwner_updatesLink() {
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));
        when(linkRepository.save(any(Link.class))).thenReturn(testLink);

        Link result = linkService.updateLink("abc123", "user1", 20);

        assertEquals(20, result.getClickLimit());
        verify(linkRepository).save(testLink);
    }

    @Test
    void updateLink_invalidOwner_throwsException() {
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));

        assertThrows(UnauthorizedAccessException.class, () ->
                linkService.updateLink("abc123", "user2", 20));
    }

    @Test
    void updateLink_nullClickLimit_doesNotUpdateLimit() {
        int originalLimit = testLink.getClickLimit();
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));
        when(linkRepository.save(any(Link.class))).thenReturn(testLink);

        linkService.updateLink("abc123", "user1", null);

        assertEquals(originalLimit, testLink.getClickLimit());
    }

    @Test
    void deleteLink_validOwner_deletesLink() {
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));

        linkService.deleteLink("abc123", "user1");

        verify(linkRepository).delete(testLink);
    }

    @Test
    void deleteLink_invalidOwner_throwsException() {
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(testLink));

        assertThrows(UnauthorizedAccessException.class, () ->
                linkService.deleteLink("abc123", "user2"));
    }
}
