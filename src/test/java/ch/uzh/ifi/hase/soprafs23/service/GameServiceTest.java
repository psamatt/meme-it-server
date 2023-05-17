package ch.uzh.ifi.hase.soprafs23.service;

import static ch.uzh.ifi.hase.soprafs23.EntityMother.buildFullGame;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaulTextBox;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultLobby;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultMeme;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultRating;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.defaultUser;
import static ch.uzh.ifi.hase.soprafs23.EntityMother.openRound;
import static org.assertj.core.api.Assertions.catchThrowable;

import ch.uzh.ifi.hase.soprafs23.entity.Game;
import ch.uzh.ifi.hase.soprafs23.entity.GameState;
import ch.uzh.ifi.hase.soprafs23.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.entity.Meme;
import ch.uzh.ifi.hase.soprafs23.entity.Rating;
import ch.uzh.ifi.hase.soprafs23.entity.Round;
import ch.uzh.ifi.hase.soprafs23.entity.Template;
import ch.uzh.ifi.hase.soprafs23.entity.TextBox;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.ImgflipClient;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.IMemeApi;
import ch.uzh.ifi.hase.soprafs23.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.repository.MemeRepository;
import ch.uzh.ifi.hase.soprafs23.repository.TextBoxRepository;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    @Mock
    private GameRepository gameRepository;
    @Mock
    private LobbyService lobbyService;
    @Mock
    private MemeRepository memeRepository;
    @Mock
    private TextBoxRepository textBoxRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IMemeApi iMemeApi;

    private GameService gameService;

    @BeforeEach
    public void setup() {
        gameService = new GameService(gameRepository, lobbyService, memeRepository, textBoxRepository, userRepository, iMemeApi);
    }

    @Test
    void createGame_validInputs_success() {
        ImgflipClient.ApiResponse apiResponse = buildIMemeAPIResponse();
        when(iMemeApi.getTemplates()).thenReturn(apiResponse);

        String lobbyCode = "lobbyCode";
        Lobby lobby = defaultLobby();
        when(lobbyService.getLobbyByCode(lobbyCode)).thenReturn(lobby);

        Game game = gameService.createGame(lobbyCode);

        verify(gameRepository).save(game);
        verify(gameRepository).flush();
        verify(lobbyService).setGameStarted(lobbyCode, game.getId(), game.getStartedAt());

        assertThat(game.getState()).isEqualTo(GameState.CREATION);
        assertThat(game.getCurrentRound()).isEqualTo(1);
        assertThat(game.getTemplates())
                .extracting("imageUrl")
                        .contains(apiResponse.data.memes.get(0).url);
        assertThat(game.getPlayers())
                .isEqualTo(lobby.getPlayers());
        assertThat(game.getRounds()).hasSize(1);
        assertThat(game.getRounds())
                .extracting("isOpen", "roundNumber")
                .contains(Tuple.tuple(true, 1));
    }

    @Test
    public void getGame_validGameId_success() {
        String gameId = "gameId";
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Game actual = gameService.getGame(gameId);

        assertThat(actual).isEqualTo(game);
    }

    @Test
    public void getGame_unknownGameId_failure() {
        String gameId = "gameId";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.getGame(gameId));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void getTemplate_validGameId_success() {
        String gameId = "gameId";
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Template actual = gameService.getTemplate(gameId, defaultUser());

        assertThat(actual).isEqualTo(game.getTemplate());
    }

    @Test
    public void getTemplate_unknownGameId_failure() {
        String gameId = "gameId";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.getTemplate(gameId, defaultUser()));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void swapTemplate_validGameId_swapLimitReached() {
        String gameId = "gameId";
        User user = defaultUser();
        user.setExecutedSwaps(100);
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Throwable t = catchThrowable(() -> gameService.swapTemplate(gameId, user));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Swap Limit already reached"));
    }

    @Test
    public void swapTemplate_validGameId_success() {
        String gameId = "gameId";
        User user = defaultUser();
        user.setExecutedSwaps(0);
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Template template = gameService.swapTemplate(gameId, user);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User actualUser = userArgumentCaptor.getValue();

        assertThat(actualUser.getExecutedSwaps()).isEqualTo(1);
        assertThat(template).isEqualTo(game.getTemplate());
    }


    @Test
    public void createMeme_validGameId_success() {
        String gameId = "gameId";
        String templateId = "templateId";

        Game game = buildFullGame(new Date(), "templateId");
        Meme meme = defaultMeme(UUID.randomUUID());

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.createMeme(gameId, templateId, meme, defaultUser());

        verify(memeRepository).save(any(Meme.class));
        verify(gameRepository).save(any(Game.class));
        verify(gameRepository).flush();
    }

    @Test
    public void createMeme_unknownGameId_failure() {
        String gameId = "gameId";
        String templateId = "templateId";

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.createMeme(gameId, templateId, defaultMeme(UUID.randomUUID()), defaultUser()));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void getMemes_validGameId_success() {
        String gameId = "gameId";
        Game game = buildFullGame(new Date(), "templateId");
        Meme meme = defaultMeme(UUID.randomUUID());
        TextBox textBox = defaulTextBox();
        when(gameRepository.findByIdWithRounds(gameId)).thenReturn(Optional.of(game));
        when(memeRepository.findAllByRound_Id(game.getRound().getId())).thenReturn(List.of(meme));
        when(textBoxRepository.findAllByMeme_Id(meme.getId())).thenReturn(List.of(textBox));

        List<Meme> memes = gameService.getMemes(gameId);

        assertThat(memes).containsExactly(meme);
        assertThat(memes).flatExtracting("textBoxes")
                .containsExactly(textBox);
    }

    @Test
    public void getMemes_unknownGameId_failure() {
        String gameId = "gameId";
        when(gameRepository.findByIdWithRounds(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.getMemes(gameId));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void createRating_unknownGameId_failure() {
        String gameId = "gameId";
        String memeId = UUID.randomUUID().toString();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.createRating(gameId, memeId, defaultRating(), defaultUser()));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void createRating_unknownMemeId_failure() {
        String gameId = "gameId";
        String unknownMemeId = UUID.randomUUID().toString();
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Throwable t = catchThrowable(() -> gameService.createRating(gameId, unknownMemeId, defaultRating(), defaultUser()));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Meme not found"));
    }

    @Test
    public void createRating_validMemeId_success() {
        String gameId = "gameId";
        UUID memeId = UUID.randomUUID();
        Game game = buildFullGame(new Date(), "templateId");
        Round round = openRound(new Date());
        round.setMemes(List.of(defaultMeme(memeId)));
        game.setRounds(Arrays.asList(round));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.createRating(gameId, memeId.toString(), defaultRating(), defaultUser());

        verify(gameRepository).save(any(Game.class));
        verify(gameRepository).flush();
    }

    @Test
    public void getRatingsFromRound_unknownGameId_failure() {
        String gameId = "gameId";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.getRatingsFromRound(gameId));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void getRatingsFromRound_validGameId_success() {
        String gameId = "gameId";
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        List<Rating> ratingsFromRound = gameService.getRatingsFromRound(gameId);

        assertThat(ratingsFromRound).isEqualTo(game.getRound().getRatings());
    }

    @Test
    public void getAllRatings_unknownGameId_failure() {
        String gameId = "gameId";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> gameService.getAllRatings(gameId));

        assertThat(t).usingRecursiveComparison()
                .isInstanceOf(ResponseStatusException.class)
                .isEqualTo(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @Test
    public void getAllRatings_validGameId_success() {
        String gameId = "gameId";
        Game game = buildFullGame(new Date(), "templateId");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        List<Rating> allRatings = gameService.getRatingsFromRound(gameId);

        List<Rating> collect = game.getRounds()
                .stream()
                .map(Round::getRatings)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertThat(allRatings).isEqualTo(collect);
    }

    private static ImgflipClient.ApiResponse buildIMemeAPIResponse() {
        ImgflipClient.ApiResponse apiResponse = new ImgflipClient.ApiResponse();
        ImgflipClient.Data data = new ImgflipClient.Data();
        ImgflipClient.Meme meme = new ImgflipClient.Meme();
        meme.url = "url";
        data.memes = List.of(meme);
        apiResponse.data = data;
        return apiResponse;
    }
}
