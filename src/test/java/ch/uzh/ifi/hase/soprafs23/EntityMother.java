package ch.uzh.ifi.hase.soprafs23;

import ch.uzh.ifi.hase.soprafs23.entity.*;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.*;

public class EntityMother {

    public static Lobby defaultLobby() {
        LobbySetting lobbySetting = new LobbySetting();
        lobbySetting.setMaxRounds(3);
        lobbySetting.setRoundDuration(60);
        lobbySetting.setRatingDuration(5);
        lobbySetting.setMemeChangeLimit(2);

        Lobby lobby = new Lobby();
        lobby.setLobbySetting(lobbySetting);
        lobby.setPlayers(List.of(defaultUser()));
        return lobby;
    }

    public static Rating defaultRating() {
        Rating r = new Rating();
        r.setRating(1);
        r.setMeme(defaultMeme(UUID.randomUUID()));
        r.setUser(defaultUser());
        return r;
    }

    public static Rating emptyRating() {
        Rating r = new Rating();
        r.setRating(1);
        return r;
    }

    public static Meme defaultMeme(UUID memeId) {
        Meme m = new Meme();
        setPrivateFieldValue(m, "id", memeId.toString());
        m.setUser(defaultUser());
        m.setTemplate(defaultTemplate("templateId"));
        m.setColor("color");
        m.setFontSize(2);
        m.setTextBoxes(List.of(defaulTextBox()));
        return m;
    }

    public static TextBox defaulTextBox() {
        TextBox t = new TextBox();
        t.setText("textbox");
        return t;
    }

    public static Template defaultTemplate(String templateId) {
        Template t = new Template();
        setPrivateFieldValue(t, "id", templateId);
        t.setImageUrl("imageURL");
        return t;
    }

    public static Round openRound(Date date) {
        Round r = new Round();
        r.setOpen(true);
        r.setMemes(new ArrayList<>());
        r.setRatings(new ArrayList<>());
        r.setStartedAt(date);
        return r;
    }

    public static Round closedRound(Date date) {
        Round r = openRound(date);
        r.setOpen(false);
        return r;
    }

    public static Game buildFullGame(Date date, String templateId) {
        Game g = new Game();
        g.setState(GameState.CREATION);
        g.setCurrentRound(1);
        g.setStartedAt(date);
        g.setGameSetting(defaultGameSetting());
        g.setPlayers(List.of(defaultUser()));
        g.setTemplates(List.of(defaultTemplate(templateId)));
        g.setRounds(Arrays.asList(openRound(date)));
        return g;
    }

    public static GameSetting defaultGameSetting() {
        GameSetting gs = new GameSetting();
        gs.setRatingDuration(1);
        gs.setMaxRounds(1);
        gs.setRatingDuration(10);
        gs.setRoundResultDuration(5);
        gs.setTemplateSwapLimit(5);
        return gs;
    }

    public static User defaultUser() {
        User u = new User();
        u.setName("Joe Bloggs");
        u.setId("123");
        return u;
    }

    private static void setPrivateFieldValue(Object o, String fieldName, Object value) {
        try {
            FieldUtils.writeField(o, fieldName, value, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
