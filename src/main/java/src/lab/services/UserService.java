package src.lab.services;

import src.lab.db.models.User;

public interface UserService {
    User getOrCreateUser(String userId);
}
