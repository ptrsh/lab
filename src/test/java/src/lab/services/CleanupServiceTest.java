package src.lab.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import src.lab.db.models.Link;
import src.lab.db.repositories.LinksRepository;
import src.lab.services.impl.CleanupServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock
    private LinksRepository linkRepository;

    @InjectMocks
    private CleanupServiceImpl cleanupService;

    private Link expiredLink1;
    private Link expiredLink2;

    @BeforeEach
    void setUp() {
        expiredLink1 = new Link();
        expiredLink1.setId(1L);
        expiredLink1.setShortCode("exp1");
        expiredLink1.setExpiresAt(LocalDateTime.now().minusHours(1));

        expiredLink2 = new Link();
        expiredLink2.setId(2L);
        expiredLink2.setShortCode("exp2");
        expiredLink2.setExpiresAt(LocalDateTime.now().minusHours(2));
    }

    @Test
    void cleanupExpiredLinks_withExpiredLinks_deletesAll() {
        List<Link> expiredLinks = Arrays.asList(expiredLink1, expiredLink2);
        when(linkRepository.findByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(expiredLinks);

        cleanupService.cleanupExpiredLinks();

        verify(linkRepository).findByExpiresAtBefore(any(LocalDateTime.class));
        verify(linkRepository).deleteAll(expiredLinks);
    }

    @Test
    void cleanupExpiredLinks_noExpiredLinks_doesNotDelete() {
        when(linkRepository.findByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        cleanupService.cleanupExpiredLinks();

        verify(linkRepository).findByExpiresAtBefore(any(LocalDateTime.class));
        verify(linkRepository, never()).deleteAll(anyList());
    }
}
