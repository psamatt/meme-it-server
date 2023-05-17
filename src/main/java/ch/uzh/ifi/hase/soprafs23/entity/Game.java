package ch.uzh.ifi.hase.soprafs23.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "GAME")
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Template> templates;

    @Column(nullable = false)
    private GameSetting gameSetting;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private List<Round> rounds;

    @Column(nullable = false)
    private Integer currentRound;

    @Column(nullable = false)
    private GameState state;

    @OneToMany(cascade = CascadeType.REFRESH)
    private List<User> players;

    @Column(nullable = false)
    private Date startedAt;

    public String getId() {
        return id;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Template getTemplate() {
        // get a random template from the list
        return templates.get((int) (Math.random() * templates.size()));
    }

    public Template getTemplateById(String id) {
        for (Template template : templates) {
            if (template.getId().equals(id)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Template with id " + id + " not found");
    }

    public void setGameSetting(GameSetting gameSetting) {
        this.gameSetting = gameSetting;
    }

    public GameSetting getGameSetting() {
        return gameSetting;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public Round getRound() {
        return rounds.get(currentRound - 1);
    }

    public Round setRound(Round round) {
        return rounds.set(currentRound - 1, round);
    }

    public void addRound(Round round) {
        this.rounds.add(round);
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }
}
