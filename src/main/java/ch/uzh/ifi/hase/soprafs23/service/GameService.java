package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.*;

import ch.uzh.ifi.hase.soprafs23.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.repository.MemeRepository;
import ch.uzh.ifi.hase.soprafs23.repository.TextBoxRepository;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.IMemeApi;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.ImgflipClient;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.ImgflipClient.ApiResponse;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class GameService {
    // private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final IMemeApi memeApi;

    private final LobbyService lobbyService;

    private final GameRepository gameRepository;
    private final MemeRepository memeRepository;
    private final TextBoxRepository textBoxRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository,
                       LobbyService lobbyService, MemeRepository memeRepository, TextBoxRepository textBoxRepository,
                       UserRepository userRepository) {
        this(gameRepository, lobbyService, memeRepository, textBoxRepository, userRepository, new ImgflipClient());
    }

    public GameService(GameRepository gameRepository, LobbyService lobbyService, MemeRepository memeRepository, TextBoxRepository textBoxRepository, UserRepository userRepository, IMemeApi iMemeApi) {

        this.gameRepository = gameRepository;
        this.lobbyService = lobbyService;
        this.memeRepository = memeRepository;
        this.textBoxRepository = textBoxRepository;
        this.userRepository = userRepository;
        memeApi = iMemeApi;
    }

    /**
     * Creates a new game
     * 
     * @param lobbyId
     * @return
     */
    public Game createGame(String lobbyCode) {
        Game newGame = new Game();
        Lobby lobby = lobbyService.getLobbyByCode(lobbyCode);

        // set templates
        ApiResponse apiResponse = memeApi.getTemplates();
        List<Template> templates = new ArrayList<Template>(apiResponse.data.memes.size());
        for (var meme : apiResponse.data.memes) {
            Template template = new Template();
            template.setImageUrl(meme.url);
            templates.add(template);
        }
        newGame.setTemplates(templates);
        newGame.setState(GameState.CREATION);

        // set game settings
        GameSetting gameSetting = new GameSetting();
        gameSetting.setMaxRounds(lobby.getLobbySetting().getMaxRounds());
        gameSetting.setRoundDuration(lobby.getLobbySetting().getRoundDuration());
        gameSetting.setRatingDuration(lobby.getLobbySetting().getRatingDuration());
        gameSetting.setTemplateSwapLimit(lobby.getLobbySetting().getMemeChangeLimit());
        gameSetting.setRoundResultDuration(20); // ! default 20 seconds
        newGame.setGameSetting(gameSetting);

        // set round
        newGame.setCurrentRound(1);

        // initialise players
        List<User> users = lobby.getPlayers();
        // shitty fix but other wise error is thrown:
        // org.hibernate.HibernateException: Found shared references to a collection
        // ! this currently duplicates the users in the db
        List<User> players = new ArrayList<User>(users.size());
        for (var user : users) {
            User player = new User();
            player.setId(user.getId());
            player.setName(user.getName());
            players.add(player);
        }
        newGame.setPlayers(players);

        // Add 2 seconds to the current time
        Calendar calendar = Calendar.getInstance();
        newGame.setStartedAt(calendar.getTime());

        // initialise rounds array
        List<Round> rounds = new ArrayList<Round>(lobby.getLobbySetting().getMaxRounds());
        newGame.setRounds(rounds);

        // initialise first round
        Round round = new Round();
        round.setOpen(true);
        round.setRoundNumber(1);
        round.setStartedAt(calendar.getTime()); // round starts same time as game
        newGame.addRound(round);

        save(newGame);

        // inform lobby that game has started
        lobbyService.setGameStarted(lobbyCode, newGame.getId(), newGame.getStartedAt());

        // TODO: figure out way to delete lobby

        return newGame;

    }

    /**
     * Returns a game by id
     * 
     * @param gameId
     * @return
     */
    public Game getGame(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        return game;
    }

    /**
     * Returns the template of a meme
     * 
     * @param gameId
     * @return
     */
    public Template getTemplate(String gameId, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        // TODO: check user has can get more templates depending on lobby rules
        // TODO: update user has gotten template

        return game.getTemplate();
    }

    /**
     *
     * @param gameId
     * @param user
     * @return
     */
    public Template swapTemplate(String gameId, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (user.getExecutedSwaps() < game.getGameSetting().getTemplateSwapLimit()) {
            user.setExecutedSwaps(user.getExecutedSwaps() + 1);
            userRepository.save(user);
            return game.getTemplate();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Swap Limit already reached");
    }

    /**
     * Submits a meme of a user
     * 
     * @param gameId
     * @param meme
     * @param user
     */
    public void createMeme(String gameId, String templateId, Meme meme, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        meme.setUser(user);
        for (TextBox t : meme.getTextBoxes()) {
            t.setMeme(meme);
        }
        Round round = game.getRound();

        // check if round still open
        /*
         * TODO: if (!round.isOpen()) {
         * throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
         * "Round is not open");
         * }
         */

        // set user chosen template
        Template template = game.getTemplateById(templateId);
        meme.setTemplate(template);

        // add meme to the round
        round.addMeme(meme);
        meme.setRound(round);
        // update round
        game.setRound(round);
        memeRepository.save(meme);
        // perist changes
        save(game);

    }

    public List<Meme> getMemes(String gameId) {
        Game game = gameRepository.findByIdWithRounds(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        Round round = game.getRound();
        List<Meme> memes = findMemesByRoundId(round.getId());

        for (Meme meme : memes) {
            List<TextBox> textBoxes = findTextBoxesByMemeId(meme.getId());
            meme.setTextBoxes(textBoxes);
        }

        return memes;
    }

    public List<Meme> findMemesByRoundId(Long roundId) {
        return memeRepository.findAllByRound_Id(roundId);
    }

    public List<TextBox> findTextBoxesByMemeId(String memeId) {
        return textBoxRepository.findAllByMeme_Id(memeId);
    }

    /**
     * Submits a rating for a meme by a user
     * 
     * @param gameId
     * @param memeId
     * @param rating
     * @param user
     */
    public void createRating(String gameId, String memeId, Rating rating, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        // TODO: check user actually part of game
        // TODO: check user has not already rated meme
        // TODO: user is not meme owner

        Round round = game.getRound();
        Meme meme = round.getMemeById(memeId);
        if (meme == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meme not found");
        }

        rating.setUser(user);
        rating.setMeme(meme);
        round.addRating(rating);

        // update round
        game.setRound(round);

        // perist changes
        save(game);
    }

    /**
     * Returns a list of ratings from the past round
     * 
     * @param gameId
     * @return
     */
    public List<Rating> getRatingsFromRound(String gameId) {
        // Return a list of players, their score and the meme of the round
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        Round round = game.getRound();

        return round.getRatings();
    }

    /**
     * Returns all ratings from the game
     * 
     * @param gameId
     * @return
     */
    public List<Rating> getAllRatings(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<Round> rounds = game.getRounds();

        List<Rating> allRatings = new ArrayList<Rating>();

        for (Round round : rounds) {
            allRatings.addAll(round.getRatings());
        }
        return allRatings;
    }

    /**
     * Perists game changes
     * 
     * @param game
     */
    public void save(Game game) {
        gameRepository.save(game);
        gameRepository.flush();
    }

}
