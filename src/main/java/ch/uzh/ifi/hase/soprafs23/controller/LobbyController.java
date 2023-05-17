package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.rest.dto.lobby.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs23.service.LobbyService;
import ch.uzh.ifi.hase.soprafs23.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.lobby.LobbyPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.lobby.LobbyMapper;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.user.UserMapper;
import ch.uzh.ifi.hase.soprafs23.rest.dto.lobby.LobbyGetDTO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Lobby Controller
 * This class is responsible for handling all REST request that are related to
 * the lobby.
 * The controller will receive the request and delegate the execution to the
 * LobbyService and finally return the result.
 */
@RestController
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LobbyGetDTO> getLobbies() {
        // get all lobbies
        List<Lobby> createdLobby = lobbyService.getLobbies();

        System.out.println(createdLobby);
        // convert internal representation of lobby back to API
        List<LobbyGetDTO> getDTOs = new ArrayList<LobbyGetDTO>();
        for (Lobby lobby : createdLobby) {
            getDTOs.add(LobbyMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby));
        }

        return getDTOs;
    }

    @PostMapping("/lobbies")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public LobbyGetDTO createLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // convert API user to internal representation
        Lobby lobbyInput = LobbyMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        // create lobby
        Lobby createdLobby = lobbyService.createLobby(lobbyInput, user);

        // convert internal representation of lobby back to API
        return LobbyMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);
    }

    // @PutMapping("/lobbies/{lobbyId}")
    // @ResponseStatus(HttpStatus.OK)
    // @ResponseBody
    // public void updateLobby(@PathVariable String lobbyId, @RequestBody
    // LobbyPutDTO lobbyPutDTO) {
    // // convert API user to internal representation
    // LobbySetting lobbySettingInput =
    // LobbyMapper.INSTANCE.convertLobbyPutDTOtoEntity(lobbyPutDTO);

    // // update lobby
    // lobbyService.updateLobby(lobbyId, lobbySettingInput);
    // }

    @PutMapping("/lobbies/{lobbyCode}")
    @ResponseStatus(HttpStatus.OK)
    public void updateLobby(@PathVariable String lobbyCode, @RequestBody LobbyPutDTO lobbyPutDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // convert API user to internal representation
        Lobby lobbyInput = LobbyMapper.INSTANCE.convertLobbyPutDTOtoEntity(lobbyPutDTO);
        // update lobby
        lobbyService.updateLobby(lobbyCode, lobbyInput, user);
    }

    @GetMapping("/lobbies/{lobbyCode}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO getLobby(@PathVariable String lobbyCode) {
        Lobby getLobby = lobbyService.getLobbyByCode(lobbyCode);
        return LobbyMapper.INSTANCE.convertEntityToLobbyGetDTO(getLobby);
    }

    // If the user is not in a lobby, they can join a lobby by entering the lobby
    // code
    // If the code provided is incorrect an error message gets displayed
    @PostMapping("/lobbies/{lobbyCode}/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO joinLobby(@PathVariable String lobbyCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Add the player to the lobby
        Lobby lobby = lobbyService.joinLobby(lobbyCode, user);

        // return lobby
        return LobbyMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
    }

    @DeleteMapping("/lobbies/{lobbyCode}/players")
    @ResponseStatus(HttpStatus.OK)
    public void leaveLobby(@PathVariable String lobbyCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // remove the player from the lobby
        lobbyService.leaveLobby(lobbyCode, user);
    }

    // Kicks user and returns updated lobby to lobby owner
    @PutMapping("/lobbies/{lobbyCode}/players")
    @ResponseStatus(HttpStatus.OK)
    public LobbyGetDTO kickLobby(@PathVariable String lobbyCode, @RequestBody UserPostDTO userPostDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        User userKicked = UserMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        // Add the player to the lobby
        Lobby lobby = lobbyService.kickPlayer(lobbyCode, user, userKicked);

        // return lobby
        return LobbyMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
    }

}