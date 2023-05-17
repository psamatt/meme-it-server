package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void createUser_validInputs_success() {
        // given
        assertNull(userRepository.findById("1"));

        User testUser = new User();
        testUser.setName("testName");
        // ! do not set id as its over written by userRepository

        // when
        User createdUser = userService.createUser(testUser);

        // then
        assertEquals(testUser.getName(), createdUser.getName());
        assertEquals(testUser.getId(), createdUser.getId());
        assertNotNull(createdUser.getId());
    }


    @Test
    public void getById_existingId_success() {
        User testUser = new User();
        testUser.setName("testName");

        User createdUser = userService.createUser(testUser);
        assertNotNull(createdUser.getId());

        User foundUser = userService.getById(createdUser.getId());
        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getName(), foundUser.getName());
    }
}
