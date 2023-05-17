package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;


public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setName("testName");

        /// when -> any object is being saved in the userRepository -> return a new user with a generated ID
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(invocation -> {
        User savedUser = invocation.getArgument(0, User.class);
        User newUserWithId = new User();
        newUserWithId.setName(savedUser.getName());
        newUserWithId.setId(UUID.randomUUID().toString());
        return newUserWithId;
        });
    }

    @Test
    public void createUser_validInputs_success() {
        // when -> any object is being save in the userRepository -> return the dummy
        // testUser
        User createdUser = userService.createUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testUser.getName(), createdUser.getName());

    }

    @Test
    public void getById_validId_success() {
        String testUserId = UUID.randomUUID().toString();
        testUser.setId(testUserId);
        Mockito.when(userRepository.findById(testUserId)).thenReturn(testUser);

        User retrievedUser = userService.getById(testUserId);

        assertNotNull(retrievedUser);
        assertEquals(testUserId, retrievedUser.getId());
    }
}
