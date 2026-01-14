package src.lab.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import src.lab.db.models.Link;
import src.lab.db.repositories.LinksRepository;
import src.lab.services.CleanupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CleanupServiceImpl implements CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupServiceImpl.class);

    private final LinksRepository linkRepository;

    public CleanupServiceImpl(LinksRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    @Scheduled(fixedRateString = "${app.cleanup.rate-minutes}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cleanupExpiredLinks() {
        List<Link> expired = linkRepository.findByExpiresAtBefore(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }

        linkRepository.deleteAll(expired);
        log.info("Cleaned up {} expired links", expired.size());
    }
}
