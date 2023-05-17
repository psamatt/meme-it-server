package ch.uzh.ifi.hase.soprafs23.rest.mapper.user;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserPostDTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * D
 * Tests if the
 * representation
 * * works.
 */
public class DTOMapperTest {
    @Test
    public void testCreateUser_fromUserPostDTO_toUser_success() {
        // create UserPostDTO
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("name");

        // MAP -> Create user
        User user = UserMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check content
        assertEquals(userPostDTO.getName(), user.getName());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // create User
        User user = new User();
        user.setName("Firstname Lastname");

        // MAP -> Create UserGetDTO
        UserGetDTO userGetDTO = UserMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // check content
        assertEquals(user.getName(), userGetDTO.getName());
    }
}
