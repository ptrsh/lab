package src.lab.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.lab.db.models.User;

@Repository
public interface UsersRepository extends JpaRepository<User, String> {
}
