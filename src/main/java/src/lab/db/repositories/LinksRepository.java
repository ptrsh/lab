package src.lab.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.lab.db.models.Link;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LinksRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByShortCode(String shortCode);

    List<Link> findByUserId(String userId);

    List<Link> findByExpiresAtBefore(LocalDateTime dateTime);
}
