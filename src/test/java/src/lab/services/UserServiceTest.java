package src.lab.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import src.lab.db.models.User;
import src.lab.db.repositories.UsersRepository;
import src.lab.services.impl.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private String testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = "550e8400-e29b-41d4-a716-446655440000";
        testUser = new User();
        testUser.setId(testUserId);
    }

    @Test
    void getOrCreateUser_existingUser_returnsUser() {
        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        User result = userService.getOrCreateUser(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(usersRepository).findById(testUserId);
        verify(usersRepository, never()).save(any());
    }

    @Test
    void getOrCreateUser_newUser_createsAndReturnsUser() {
        when(usersRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(usersRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.getOrCreateUser(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(usersRepository).findById(testUserId);
        verify(usersRepository).save(any(User.class));
    }
}
