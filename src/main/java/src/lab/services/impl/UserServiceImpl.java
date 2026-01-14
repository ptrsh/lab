package src.lab.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import src.lab.db.models.User;
import src.lab.db.repositories.UsersRepository;
import src.lab.services.UserService;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional
    public User getOrCreateUser(String userId) {
        Optional<User> user = usersRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }

        User newUser = new User();
        newUser.setId(userId);
        return usersRepository.save(newUser);
    }
}
