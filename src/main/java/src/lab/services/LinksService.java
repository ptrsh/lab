package src.lab.services;

import src.lab.db.models.Link;

import java.util.List;

public interface LinksService {
    Link createShortLink(String userId, String originalUrl, Integer clickLimit);

    String redirect(String shortCode);

    List<Link> getUserLinks(String userId);

    Link getLink(String shortCode);

    Link updateLink(String shortCode, String userId, Integer clickLimit);

    void deleteLink(String shortCode, String userId);
}
