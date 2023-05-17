package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.security.JwtSecurityConfig;
import ch.uzh.ifi.hase.soprafs23.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.entity.LobbySetting;
import ch.uzh.ifi.hase.soprafs23.entity.Message;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.lobby.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// /**
// * LobbyControllerTest
// * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
// * request without actually sending them over the network.
// * This tests if the LobbyController works.
// */
@WebMvcTest(controllers = LobbyController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                JwtSecurityConfig.class
})
public class LobbyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private LobbyService lobbyService;

        @Test
        public void givenLobbies_whenGetLobbies_thenReturnJsonArray() throws Exception {
                // given

                User user = new User();
                user.setName("owner name");

                Lobby lobby = new Lobby();

                LobbySetting lobbySetting = new LobbySetting(
                                false,
                                1,
                                2,
                                3,
                                4,
                                5,
                                6,
                                7);

                lobby.setName("lobby name");
                lobby.setOwner(user);
                lobby.setLobbySetting(lobbySetting);
                lobby.setPlayers(new ArrayList<User>());
                lobby.setKickedPlayers(new ArrayList<User>());
                lobby.setMessages(new ArrayList<Message>());
                lobby.setIsJoinable(true);

                List<Lobby> allLobbies = Collections.singletonList(lobby);

                // this mocks the UserService -> we define above what the userService should
                // return when getUsers() is called
                given(lobbyService.getLobbies()).willReturn(allLobbies);

                // when
                MockHttpServletRequestBuilder getRequest = get("/lobbies").contentType(MediaType.APPLICATION_JSON);

                // then
                mockMvc.perform(getRequest).andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].name", is(lobby.getName())))
                                .andExpect(jsonPath("$[0].owner.name", is(lobby.getOwner().getName())))
                                .andExpect(jsonPath("$[0].lobbySetting.isPublic",
                                                is(lobby.getLobbySetting().getIsPublic())))
                                .andExpect(jsonPath("$[0].lobbySetting.maxPlayers",
                                                is(lobby.getLobbySetting().getMaxPlayers())))
                                .andExpect(jsonPath("$[0].lobbySetting.maxRounds",
                                                is(lobby.getLobbySetting().getMaxRounds())))
                                .andExpect(
                                                jsonPath("$[0].lobbySetting.memeChangeLimit",
                                                                is(lobby.getLobbySetting().getMemeChangeLimit())))
                                .andExpect(
                                                jsonPath("$[0].lobbySetting.superLikeLimit",
                                                                is(lobby.getLobbySetting().getSuperLikeLimit())))
                                .andExpect(jsonPath("$[0].lobbySetting.superDislikeLimit",
                                                is(lobby.getLobbySetting().getSuperDislikeLimit())))
                                .andExpect(
                                                jsonPath("$[0].lobbySetting.roundDuration",
                                                                is(lobby.getLobbySetting().getRoundDuration())))
                                .andExpect(jsonPath("$[0].lobbySetting.ratingDuration",
                                                is(lobby.getLobbySetting().getRatingDuration())));
        }

        @Test
        public void createLobby_validInput_userCreated() throws Exception {
                // given
                User user = new User();
                user.setName("owner name");

                UserPostDTO userPostDTO = new UserPostDTO();
                userPostDTO.setName("owner name");

                Lobby lobby = new Lobby();

                LobbySetting lobbySetting = new LobbySetting(
                                false,
                                1,
                                2,
                                3,
                                4,
                                5,
                                6,
                                7);

                lobby.setName("lobby name");
                lobby.setOwner(user);
                lobby.setLobbySetting(lobbySetting);
                lobby.setPlayers(new ArrayList<User>());
                lobby.setKickedPlayers(new ArrayList<User>());
                lobby.setMessages(new ArrayList<Message>());
                lobby.setIsJoinable(true);

                LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
                lobbyPostDTO.setName("lobby name");
                lobbyPostDTO.setIsPublic(false);
                lobbyPostDTO.setMaxPlayers(1);
                lobbyPostDTO.setMaxRounds(2);
                lobbyPostDTO.setMemeChangeLimit(3);
                lobbyPostDTO.setSuperLikeLimit(4);
                lobbyPostDTO.setSuperDislikeLimit(5);
                lobbyPostDTO.setRoundDuration(6);
                lobbyPostDTO.setRatingDuration(7);

                given(lobbyService.createLobby(Mockito.any(), Mockito.any())).willReturn(lobby);

                givenUserIsAuthenticated(user);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder postRequest = post("/lobbies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(lobbyPostDTO));

                // then
                mockMvc.perform(postRequest)
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name", is(lobby.getName())))
                                .andExpect(jsonPath("$.owner.name", is(lobby.getOwner().getName())))
                                .andExpect(jsonPath("$.lobbySetting.isPublic",
                                                is(lobby.getLobbySetting().getIsPublic())))
                                .andExpect(jsonPath("$.lobbySetting.maxPlayers",
                                                is(lobby.getLobbySetting().getMaxPlayers())))
                                .andExpect(jsonPath("$.lobbySetting.maxRounds",
                                                is(lobby.getLobbySetting().getMaxRounds())))
                                .andExpect(jsonPath("$.lobbySetting.memeChangeLimit",
                                                is(lobby.getLobbySetting().getMemeChangeLimit())))
                                .andExpect(jsonPath("$.lobbySetting.superLikeLimit",
                                                is(lobby.getLobbySetting().getSuperLikeLimit())))
                                .andExpect(jsonPath("$.lobbySetting.superDislikeLimit",
                                                is(lobby.getLobbySetting().getSuperDislikeLimit())))
                                .andExpect(jsonPath("$.lobbySetting.roundDuration",
                                                is(lobby.getLobbySetting().getRoundDuration())))
                                .andExpect(jsonPath("$.lobbySetting.ratingDuration",
                                                is(lobby.getLobbySetting().getRatingDuration())));

        }

        private static void givenUserIsAuthenticated(User user) {
                Authentication authentication = Mockito.mock(Authentication.class);
                Mockito.when(authentication.getPrincipal()).thenReturn(user);
                SecurityContext securityContext = Mockito.mock(SecurityContext.class);
                Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);
        }

        @Test
        public void givenLobbies_whenGetLobby_thenReturnJsonArray() throws Exception {
                // given
                User user = new User();
                user.setName("owner name");

                Lobby lobby = new Lobby();

                LobbySetting lobbySetting = new LobbySetting(
                                false,
                                1,
                                2,
                                3,
                                4,
                                5,
                                6,
                                7);
                lobby.setId(1L);
                lobby.setName("lobby name");
                lobby.setOwner(user);
                lobby.setLobbySetting(lobbySetting);
                lobby.setPlayers(new ArrayList<User>());
                lobby.setKickedPlayers(new ArrayList<User>());
                lobby.setMessages(new ArrayList<Message>());
                lobby.setIsJoinable(true);

                // this mocks the UserService -> we define above what the userService should
                // return when getUsers() is called
                given(lobbyService.getLobbyByCode(Mockito.any())).willReturn(lobby);

                // when
                MockHttpServletRequestBuilder getRequest = get("/lobbies/1").contentType(MediaType.APPLICATION_JSON);

                // then
                mockMvc.perform(getRequest).andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is(lobby.getName())))
                                .andExpect(jsonPath("$.owner.name", is(lobby.getOwner().getName())))
                                .andExpect(jsonPath("$.lobbySetting.isPublic",
                                                is(lobby.getLobbySetting().getIsPublic())))
                                .andExpect(jsonPath("$.lobbySetting.maxPlayers",
                                                is(lobby.getLobbySetting().getMaxPlayers())))
                                .andExpect(jsonPath("$.lobbySetting.maxRounds",
                                                is(lobby.getLobbySetting().getMaxRounds())))
                                .andExpect(jsonPath("$.lobbySetting.memeChangeLimit",
                                                is(lobby.getLobbySetting().getMemeChangeLimit())))
                                .andExpect(jsonPath("$.lobbySetting.superLikeLimit",
                                                is(lobby.getLobbySetting().getSuperLikeLimit())))
                                .andExpect(jsonPath("$.lobbySetting.superDislikeLimit",
                                                is(lobby.getLobbySetting().getSuperDislikeLimit())))
                                .andExpect(jsonPath("$.lobbySetting.roundDuration",
                                                is(lobby.getLobbySetting().getRoundDuration())))
                                .andExpect(jsonPath("$.lobbySetting.ratingDuration",
                                                is(lobby.getLobbySetting().getRatingDuration())));

        }

        /**
         * Helper Method to convert userPostDTO into a JSON string such that the input
         * can be processed
         * Input will look like this: {"name": "Test User", "username":
         * "testUsername"}
         *
         * @param object
         * @return string
         */
        private String asJsonString(final Object object) {
                try {
                        return new ObjectMapper().writeValueAsString(object);
                } catch (JsonProcessingException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        String.format("The request body could not be created.%s", e.toString()));
                }
        }
}