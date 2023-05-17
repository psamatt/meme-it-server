package ch.uzh.ifi.hase.soprafs23.rest.dto.lobby;

import java.util.Date;
import java.util.List;

import ch.uzh.ifi.hase.soprafs23.entity.LobbySetting;
import ch.uzh.ifi.hase.soprafs23.entity.Message;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserGetDTO;

public class LobbyGetDTO {
    private String code;

    private String name;

    private UserGetDTO owner;

    private LobbySetting lobbySetting;

    private List<UserGetDTO> players;

    private List<Message> messages;

    private String gameId;

    private Date gameStartedAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserGetDTO getOwner() {
        return owner;
    }

    public void setOwner(UserGetDTO owner) {
        this.owner = owner;
    }

    public LobbySetting getLobbySetting() {
        return lobbySetting;
    }

    public void setLobbySetting(LobbySetting lobbySetting) {
        this.lobbySetting = lobbySetting;
    }

    public List<UserGetDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<UserGetDTO> players) {
        this.players = players;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Date getGameStartedAt() {
        return gameStartedAt;
    }

    public void setGameStartedAt(Date gameStartedAt) {
        this.gameStartedAt = gameStartedAt;
    }

}
