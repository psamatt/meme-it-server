package ch.uzh.ifi.hase.soprafs23.rest.dto.game;

import java.util.Date;
import java.util.List;

import ch.uzh.ifi.hase.soprafs23.entity.GameState;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserGetDTO;

public class GameGetDTO {

    private String id;
    private GameState gameState;

    private Integer roundDuration;
    private Integer votingDuration;
    private Integer roundResultDuration;

    private Integer currentRound;

    private Integer totalRounds;

    private Date roundStartedAt;
    private Date startedAt;
    private List<UserGetDTO> players;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Integer getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(Integer roundDuration) {
        this.roundDuration = roundDuration;
    }

    public Integer getVotingDuration() {
        return votingDuration;
    }

    public void setVotingDuration(Integer votingDuration) {
        this.votingDuration = votingDuration;
    }

    public Integer getRoundResultDuration() {
        return roundResultDuration;
    }

    public void setRoundResultDuration(Integer roundResultDuration) {
        this.roundResultDuration = roundResultDuration;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }

    public Date getRoundStartedAt() {
        return roundStartedAt;
    }

    public void setRoundStartedAt(Date roundStartedAt) {
        this.roundStartedAt = roundStartedAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public List<UserGetDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<UserGetDTO> players) {
        this.players = players;
    }
}
