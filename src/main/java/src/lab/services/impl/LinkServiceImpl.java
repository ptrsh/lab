package src.lab.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import src.lab.db.models.Link;
import src.lab.db.models.User;
import src.lab.db.repositories.LinksRepository;
import src.lab.exceptions.*;
import src.lab.infra.settings.AppConfig;
import src.lab.services.LinksService;
import src.lab.services.UserService;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LinkServiceImpl implements LinksService {

    private static final Logger log = LoggerFactory.getLogger(LinkServiceImpl.class);
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int MAX_COLLISION_ATTEMPTS = 10;

    private final LinksRepository linkRepository;
    private final UserService userService;
    private final AppConfig config;

    public LinkServiceImpl(LinksRepository linkRepository, UserService userService, AppConfig config) {
        this.linkRepository = linkRepository;
        this.userService = userService;
        this.config = config;
    }

    @Override
    @Transactional
    public Link createShortLink(String userId, String originalUrl, Integer clickLimit) {
        validateUrl(originalUrl);

        User user = userService.getOrCreateUser(userId);

        Link link = new Link();
        link.setShortCode(generateUniqueShortCode(userId, originalUrl));
        link.setOriginalUrl(originalUrl);
        link.setUser(user);
        link.setClickLimit(clickLimit != null ? clickLimit : config.getLink().getDefaultClickLimit());
        link.setExpiresAt(LocalDateTime.now().plusHours(config.getLink().getTtlHours()));

        return linkRepository.save(link);
    }

    @Override
    @Transactional
    public String redirect(String shortCode) {
        Link link = linkRepository.findByShortCode(shortCode).orElseThrow(() -> new LinkNotFoundException(shortCode));

        if (link.isExpired()) {
            log.warn("Attempt to access expired link: shortCode={}, expiresAt={}, userId={}", shortCode, link.getExpiresAt(), link.getUser().getId());
            throw new LinkExpiredException(shortCode);
        }

        if (link.isLimitReached()) {
            log.warn("Attempt to access link with exceeded click limit: shortCode={}, clickCount={}, clickLimit={}, userId={}", shortCode, link.getClickCount(), link.getClickLimit(), link.getUser().getId());
            throw new LinkLimitReachedException(shortCode);
        }

        link.setClickCount(link.getClickCount() + 1);
        linkRepository.save(link);

        return link.getOriginalUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> getUserLinks(String userId) {
        return linkRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Link getLink(String shortCode) {
        return linkRepository.findByShortCode(shortCode).orElseThrow(() -> new LinkNotFoundException(shortCode));
    }

    @Override
    @Transactional
    public Link updateLink(String shortCode, String userId, Integer clickLimit) {
        Link link = getLink(shortCode);

        if (!link.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to modify this link");
        }

        if (clickLimit != null) {
            link.setClickLimit(clickLimit);
        }

        return linkRepository.save(link);
    }

    @Override
    @Transactional
    public void deleteLink(String shortCode, String userId) {
        Link link = getLink(shortCode);

        if (!link.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to delete this link");
        }

        linkRepository.delete(link);
    }

    private String generateUniqueShortCode(String userId, String originalUrl) {
        String baseCode = generateShortCode(userId, originalUrl);

        if (linkRepository.findByShortCode(baseCode).isEmpty()) {
            return baseCode;
        }

        for (int attempt = 1; attempt < MAX_COLLISION_ATTEMPTS; attempt++) {
            String code = generateShortCode(userId, originalUrl + attempt);
            if (linkRepository.findByShortCode(code).isEmpty()) {
                return code;
            }
        }

        throw new ShortCodeGenerationException();
    }

    private String generateShortCode(String userId, String originalUrl) {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ShortCodeGenerationException();
        }

        String input = userId + originalUrl;
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return encodeToURLSafeBase62(hash, config.getLink().getShortCodeLength());
    }

    private String encodeToURLSafeBase62(byte[] bytes, int length) {
        StringBuilder result = new StringBuilder();

        long value = 0;
        for (int i = 0; i < Math.min(bytes.length, 8); i++) {
            value = (value << 8) | (bytes[i] & 0xFF);
        }

        value = Math.abs(value);

        for (int i = 0; i < length; i++) {
            result.append(BASE62_CHARS.charAt((int) (value % 62)));
            value /= 62;
        }

        return result.toString();
    }

    private void validateUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException(url);
        }
    }
}
