package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.Game;
import ch.uzh.ifi.hase.soprafs23.entity.Meme;
import ch.uzh.ifi.hase.soprafs23.entity.Rating;
import ch.uzh.ifi.hase.soprafs23.entity.Template;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.job.GameJob;
import ch.uzh.ifi.hase.soprafs23.security.JwtSecurityConfig;
import ch.uzh.ifi.hase.soprafs23.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static ch.uzh.ifi.hase.soprafs23.DtoMother.buildMemePostDTO;
import static ch.uzh.ifi.hase.soprafs23.DtoMother.buildRatingPostDTO;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.buildFullGame;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultMeme;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultRating;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultTemplate;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultUser;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.emptyRating;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GameController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                JwtSecurityConfig.class
        })
public class GameControllerTest {
    private static final String FORMATTED_DATE = "2023-04-01T01:00:00.000+00:00";
    private static final Date DATE = Date.from(Instant.parse(FORMATTED_DATE));
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameJob gameJob;

    @Test
    public void givenLobbyCode_whenCreateGame_thenReturnCreatedGame() throws Exception {
        String lobbyCode = "testCode";
        Game game = buildFullGame(DATE, "templateId");

        when(gameService.createGame(lobbyCode)).thenReturn(game);

        mockMvc.perform(post("/games/{lobbyCode}", lobbyCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpectAll(verifyGameResponseBody(game));
    }

    @Test
    public void givenGameId_whenGetGame_thenReturnGame() throws Exception {
        String gameId = UUID.randomUUID().toString();
        Game game = buildFullGame(DATE, "templateId");

        when(gameService.getGame(gameId)).thenReturn(game);

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpectAll(verifyGameResponseBody(game));
    }

    @Test
    public void givenTemplateId_whenGetTemplate_thenReturnTemplate() throws Exception {
        String gameId = UUID.randomUUID().toString();
        User user = defaultUser();
        Template template = defaultTemplate("templateId");

        givenUserIsAuthenticated(user);

        when(gameService.getTemplate(gameId, user)).thenReturn(template);

        mockMvc.perform(get("/games/{gameId}/template", gameId))
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("id", is(template.getId())),
                        jsonPath("imageUrl", is(template.getImageUrl())));
    }

    @Test
    public void givenTemplateId_whenSwapTemplate_thenReturnTemplate() throws Exception {
        String gameId = UUID.randomUUID().toString();
        User user = defaultUser();
        Template template = defaultTemplate("templateId");

        givenUserIsAuthenticated(user);

        when(gameService.swapTemplate(gameId, user)).thenReturn(template);

        mockMvc.perform(put("/games/{gameId}/template", gameId))
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("id", is(template.getId())),
                        jsonPath("imageUrl", is(template.getImageUrl())));
    }

    @Test
    public void givenGameIdTemplateIdAndMeme_whenCreateMeme_thenReturnCreatedStatus() throws Exception {
        String gameId = UUID.randomUUID().toString();
        String templateId = UUID.randomUUID().toString();
        User user = defaultUser();
        Meme meme = defaultMeme(UUID.randomUUID());
        meme.setTemplate(null);
        meme.setUser(null);

        givenUserIsAuthenticated(user);

        mockMvc.perform(post("/games/{gameId}/meme/{templateId}", gameId, templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_WRITER.writeValueAsString(buildMemePostDTO())))
                .andExpect(status().isCreated());

        ArgumentCaptor<Meme> memeArgumentCaptor = ArgumentCaptor.forClass(Meme.class);
        verify(gameService).createMeme(eq(gameId), eq(templateId), memeArgumentCaptor.capture(), eq(user));
        assertThat(memeArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(meme);
    }

    @Test
    public void givenGameId_whenGetMemes_thenReturnMemes() throws Exception {
        String gameId = UUID.randomUUID().toString();
        Meme meme = defaultMeme(UUID.randomUUID());

        when(gameService.getMemes(gameId)).thenReturn(List.of(meme));

        mockMvc.perform(get("/games/{gameId}/meme", gameId))
                .andExpect(status().isCreated()) //TODO: is this correct? Should it not be "OK"
                .andExpectAll(jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(meme.getId().toString())),
                        jsonPath("$[0].imageUrl", is(meme.getTemplate().getImageUrl())),
                        jsonPath("$[0].textBoxes", hasSize(1)),
                        jsonPath("$[0].textBoxes[0].text", is(meme.getTextBoxes().get(0).getText())));
                        jsonPath("$[0].textBoxes[0].x", is(nullValue()));
                        jsonPath("$[0].textBoxes[0].y", is(nullValue()));
    }

    @Test
    public void givenGameIdMemeIdAndRating_whenCreateRating_thenReturnOkStatus() throws Exception {
        String gameId = UUID.randomUUID().toString();
        UUID memeId = UUID.randomUUID();
        User user = defaultUser();
        Rating rating = emptyRating();

        givenUserIsAuthenticated(user);

        mockMvc.perform(post("/games/{gameId}/rating/{memeId}", gameId, memeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_WRITER.writeValueAsString(buildRatingPostDTO())))
                .andExpect(status().isOk()); // TODO: is this correct? Should it not be "Created"

        ArgumentCaptor<Rating> argumentCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(gameService).createRating(eq(gameId), eq(memeId.toString()), argumentCaptor.capture(), eq(user));
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison()
                .isEqualTo(rating);
    }

    @Test
    public void givenGameId_whenGetRatingsFromRound_thenRatings() throws Exception {
        String gameId = UUID.randomUUID().toString();
        Rating rating = defaultRating();

        when(gameService.getRatingsFromRound(gameId)).thenReturn(List.of(rating));

        mockMvc.perform(get("/games/{gameId}/results/round", gameId))
                .andExpect(status().isOk())
                .andExpectAll(verifyRatingResponseBody(rating));
    }

    @Test
    public void givenGameId_whenGetAllRatings_thenRatings() throws Exception {
        String gameId = UUID.randomUUID().toString();
        Rating rating = defaultRating();

        when(gameService.getAllRatings(gameId)).thenReturn(List.of(rating));

        mockMvc.perform(get("/games/{gameId}/results/game", gameId))
                .andExpect(status().isOk())
                .andExpectAll(verifyRatingResponseBody(rating));
    }

    private static void givenUserIsAuthenticated(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private static ResultMatcher[] verifyRatingResponseBody(Rating rating) {
        return new ResultMatcher[]{jsonPath("$", hasSize(1)),
                jsonPath("$[0].rating", is(rating.getRating())),
                jsonPath("$[0].user.id", is(rating.getUser().getId())),
                jsonPath("$[0].user.name", is(rating.getUser().getName())),
                jsonPath("$[0].meme.id", is(rating.getMeme().getId().toString())),
                jsonPath("$[0].meme.imageUrl", is(nullValue())),
                jsonPath("$[0].meme.textBoxes", hasSize(1)),
                jsonPath("$[0].meme.textBoxes[0].text", is(rating.getMeme().getTextBoxes().get(0).getText())),
                jsonPath("$[0].meme.textBoxes[0].xRate", is(rating.getMeme().getTextBoxes().get(0).getxRate())),
                jsonPath("$[0].meme.textBoxes[0].yRate", is(rating.getMeme().getTextBoxes().get(0).getyRate())),
                jsonPath("$[0].meme.fontSize", is(rating.getMeme().getFontSize())),
                jsonPath("$[0].meme.color", is(rating.getMeme().getColor())),
                jsonPath("$[0].meme.backgroundColor", is(rating.getMeme().getBackgroundColor())),
                jsonPath("$[0].meme.user.id", is(rating.getMeme().getUser().getId())),
                jsonPath("$[0].meme.user.name", is(rating.getMeme().getUser().getName())),
                jsonPath("$[0].meme.user.executedSwaps", is(rating.getMeme().getUser().getExecutedSwaps()))};
    }

    private static ResultMatcher[] verifyGameResponseBody(Game game) {
        User firstUser = game.getPlayers().get(0);

        return new ResultMatcher[]{
                jsonPath("id", is(game.getId())),
                jsonPath("gameState", is(game.getState().toString())),
                jsonPath("roundDuration", is(game.getGameSetting().getRoundDuration())),
                jsonPath("votingDuration", is(game.getGameSetting().getRatingDuration())),
                jsonPath("roundResultDuration", is(game.getGameSetting().getRoundResultDuration())),
                jsonPath("currentRound", is(game.getCurrentRound())),
                jsonPath("totalRounds", is(game.getGameSetting().getMaxRounds())),
                jsonPath("roundStartedAt", is(FORMATTED_DATE)),
                jsonPath("startedAt", is(FORMATTED_DATE)),
                jsonPath("players", hasSize(1)),
                jsonPath("players[0].name", is(firstUser.getName())),
                jsonPath("players[0].id", is(firstUser.getId()))};
    }
}