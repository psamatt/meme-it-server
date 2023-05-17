package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

@Entity
@Table(name = "LOBBY")
public class Lobby implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @OneToOne
    private User owner;

    @Column(nullable = false)
    private LobbySetting lobbySetting;

    // ! no "@Column(nullable = false)" for foreign keys!
    @OneToMany
    private List<User> players;

    @OneToMany
    private List<User> kickedPlayers;

    @OneToMany
    private List<Message> messages;

    @Column(nullable = false)
    private boolean isJoinable;

    private Date gameStartedAt;

    private String gameId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LobbySetting getLobbySetting() {
        return lobbySetting;
    }

    public void setLobbySetting(LobbySetting lobbySetting) {
        this.lobbySetting = lobbySetting;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    // Help-function to check if a user exists in a list of users
    public boolean containsUser(Iterable<User> users, User user) {
        for (User u : users) {
            if (u.equals(user)) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(User player) {
        if (this.players == null) {
            this.players = new ArrayList<User>();
        }

        if (this.kickedPlayers == null) {
            this.kickedPlayers = new ArrayList<User>();
        }

        // check if player is already in lobby
        if (containsUser(this.players, player)) {
            throw new IllegalArgumentException("Player is already in the lobby");
        }

        // check if player is kicked
        if (containsUser(this.kickedPlayers, player)) {
            throw new IllegalArgumentException("Player is kicked and therefore not allowed to join");
        }

        // check if player has a name set
        if (player.getName() == null || player.getName().isEmpty()) {
            throw new IllegalArgumentException("Player has no name set yet");
        }

        this.players.add(player);
    }

    public void removePlayer(User player) {
        this.players.remove(player);
    }

    public List<User> getKickedPlayers() {
        return kickedPlayers;
    }

    public void setKickedPlayers(List<User> kickedPlayers) {
        this.kickedPlayers = kickedPlayers;
    }

    public void addKickedPlayer(User kickedPlayer) {
        this.kickedPlayers.add(kickedPlayer);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public boolean isJoinable() {
        return isJoinable;
    }

    public void setIsJoinable(boolean isJoinable) {
        this.isJoinable = isJoinable;
    }

    public boolean isFull() {

        Long totalUsers = StreamSupport.stream(this.players.spliterator(), true).count();

        return this.lobbySetting.getMaxPlayers() == totalUsers.intValue();

    }

    public Date getGameStartedAt() {
        return gameStartedAt;
    }

    public void setGameStartedAt(Date gameStartedAt) {
        this.gameStartedAt = gameStartedAt;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

}
