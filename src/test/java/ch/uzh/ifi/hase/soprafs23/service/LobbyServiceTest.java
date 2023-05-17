package ch.uzh.ifi.hase.soprafs23.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.entity.LobbySetting;
import ch.uzh.ifi.hase.soprafs23.repository.LobbyRepository;

public class LobbyServiceTest {
    @Mock
    private LobbyRepository lobbyRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private Lobby lobby;

    @BeforeEach  // to be executed before each test case
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        User user = new User();
        user.setName("owner name");

        lobby = new Lobby();
        lobby.setCode("123abc");
        lobby.setName("lobby name");
        lobby.setOwner(user);
        lobby.setIsJoinable(true);


        // when -> any object is being save in the lobbyRepository -> return the dummy lobby
        Mockito.when(lobbyRepository.save(Mockito.any())).thenReturn(lobby);
    }

    
    @Test
    public void createLobby_validInputs_success() {

        // given
        User user = new User();
        user.setName("owner name");
        //user.setUuid("1");

        // when -> any object is being save in the userRepository -> return the dummy
        // testUser
        Lobby createdLobby = lobbyService.createLobby(lobby, user);

        // then
        Mockito.verify(lobbyRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(lobby.getName(), createdLobby.getName());
        assertEquals(lobby.getOwner(), createdLobby.getOwner());

    }


    @Test
    public void updateLobby_validInputs_success() {
        Lobby updatedLobby = new Lobby();
        updatedLobby.setName("updated lobby name");
        updatedLobby.setLobbySetting(new LobbySetting());
        User owner = lobby.getOwner();

        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);
        Mockito.when(lobbyRepository.save(Mockito.any())).thenReturn(updatedLobby);

        Lobby result = lobbyService.updateLobby(lobby.getCode(), updatedLobby, owner);

        Mockito.verify(lobbyRepository, Mockito.times(1)).findByCode(Mockito.anyString());
        Mockito.verify(lobbyRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(updatedLobby.getName(), result.getName());
}



    @Test
    public void getLobbyByCode_validCode_success() {
        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);

        Lobby result = lobbyService.getLobbyByCode(lobby.getCode());

        Mockito.verify(lobbyRepository, Mockito.times(1)).findByCode(Mockito.anyString());
        assertEquals(lobby, result);
    }



    @Test
    public void joinLobby_validInputs_success() {
        User joiningUser = new User();
        //joiningUser.setId(2L);
        joiningUser.setName("joiningUser");
        lobby.setPlayers(new ArrayList<>());
        lobby.setKickedPlayers(new ArrayList<>());
    
        LobbySetting setting = new LobbySetting();
        setting.setMaxPlayers(2);
        lobby.setLobbySetting(setting);
        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);

        Lobby result = lobbyService.joinLobby(lobby.getCode(), joiningUser);

        Mockito.verify(lobbyRepository, Mockito.times(1)).findByCode(Mockito.anyString());
        Mockito.verify(lobbyRepository, Mockito.times(1)).save(Mockito.any());
        assertTrue(result.getPlayers().contains(joiningUser));
}




    @Test
    public void leaveLobby_validInputs_success() {
        User leavingUser = new User();
        leavingUser.setId("ID");
        leavingUser.setName("leavingUser");
        lobby.setPlayers(new ArrayList<>());
        lobby.setKickedPlayers(new ArrayList<>());
        lobby.addPlayer(leavingUser);
        lobby.setOwner(leavingUser);

        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);
        Mockito.doNothing().when(lobbyRepository).delete(Mockito.any());

        lobbyService.leaveLobby(lobby.getCode(), leavingUser);

        Mockito.verify(lobbyRepository, Mockito.times(1)).findByCode(Mockito.anyString());
        Mockito.verify(lobbyRepository, Mockito.times(1)).delete(Mockito.any());
        assertTrue(lobby.getPlayers().contains(leavingUser));
    }



    @Test
    public void kickPlayer_validInputs_success() {
        User owner = lobby.getOwner();
        User kickedUser = new User();
        //kickedUser.setId(2L);
        kickedUser.setName("kickedUser");
        lobby.setPlayers(new ArrayList<>());
        lobby.setKickedPlayers(new ArrayList<>());
        lobby.addPlayer(kickedUser);

        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);

        Lobby result = lobbyService.kickPlayer(lobby.getCode(), owner, kickedUser);

        Mockito.verify(lobbyRepository, Mockito.times(1)).findByCode(Mockito.anyString());
        Mockito.verify(lobbyRepository, Mockito.times(1)).save(Mockito.any());
        assertFalse(result.getPlayers().contains(kickedUser));
    }

    @Test
    public void joinLobby_lobbyFull_failure() {
        User joiningUser = new User();
        joiningUser.setName("Joining User");

        LobbySetting setting = new LobbySetting();
        setting.setMaxPlayers(1);
        lobby.setLobbySetting(setting);

        // Add a player to the lobby to make it full
        User existingPlayer = new User();
        existingPlayer.setName("Existing Player");
        List<User> players = new ArrayList<>();
        players.add(existingPlayer);
        lobby.setPlayers(players);

        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);

        assertThrows(ResponseStatusException.class, () -> lobbyService.joinLobby(lobby.getCode(), joiningUser));
    }

    @Test
    public void kickPlayer_notOwner_failure() {
        User notOwner = new User();
        notOwner.setName("Not Owner");
        User userToKick = new User();
        userToKick.setName("User To Kick");
        lobby.setPlayers(new ArrayList<>());
        lobby.setKickedPlayers(new ArrayList<>());
        lobby.addPlayer(userToKick);

        Mockito.when(lobbyRepository.findByCode(Mockito.anyString())).thenReturn(lobby);

        assertThrows(ResponseStatusException.class, () -> lobbyService.kickPlayer(lobby.getCode(), notOwner, userToKick));
    }
    
}
