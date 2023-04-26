package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Game;
import ch.uzh.ifi.hase.soprafs23.entity.GameSetting;
import ch.uzh.ifi.hase.soprafs23.entity.GameState;
import ch.uzh.ifi.hase.soprafs23.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.entity.Meme;
import ch.uzh.ifi.hase.soprafs23.entity.Player;
import ch.uzh.ifi.hase.soprafs23.entity.PlayerState;

import ch.uzh.ifi.hase.soprafs23.entity.Rating;
import ch.uzh.ifi.hase.soprafs23.entity.Round;
import ch.uzh.ifi.hase.soprafs23.entity.Template;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.IMemeApi;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.ImgflipClient;
import ch.uzh.ifi.hase.soprafs23.utility.memeapi.ImgflipClient.ApiResponse;

import java.time.LocalDateTime;
import java.util.*;

import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class GameService {
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    private final IMemeApi memeApi;

    private final LobbyService lobbyService;

    private final GameRepository gameRepository;

    private final JobScheduler jobScheduler;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository, JobScheduler jobScheduler,
            LobbyService lobbyService) {
        this(gameRepository, jobScheduler, lobbyService, new ImgflipClient());
    }

    public GameService(GameRepository gameRepository, JobScheduler jobScheduler,
                       LobbyService lobbyService, IMemeApi memeApi) {
        this.gameRepository = gameRepository;
        this.jobScheduler = jobScheduler;
        this.lobbyService = lobbyService;
        this.memeApi = memeApi;
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
        newGame.setGameSetting(gameSetting);

        // set round
        newGame.setCurrentRound(1);

        // initialise players
        List<User> users = lobby.getPlayers();
        List<Player> players = new ArrayList<Player>(users.size());
        for (User user : users) {
            Player player = new Player();

            player.setUser(user);
            player.setState(PlayerState.READY);

            players.add(player);
        }
        newGame.setPlayers(players);
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Add 2 seconds to the current time
        calendar.setTime(currentDate);
        calendar.add(Calendar.SECOND, 5);

        newGame.setStartedAt(calendar.getTime());

        // initialise first round
        List<Round> rounds = new ArrayList<Round>(lobby.getLobbySetting().getMaxRounds());
        Round round = new Round();
        round.setOpen(true);
        LocalDateTime now = LocalDateTime.now();
        round.setStartedAt(now);
        round.setRoundNumber(1);
        rounds.add(0, round);

        newGame.setRounds(rounds);

        save(newGame);

        // start game job
        // * game job takes care of updating game state
        // ! Bug with the game job
        // jobScheduler.enqueue(() -> exectue(newGame.getId()));

        lobbyService.setGameStarted(lobbyCode, newGame.getId(), newGame.getStartedAt());

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

        Round round = game.getRound();

        // check if round still open
        if (!round.isOpen()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Round is not open");
        }

        // set user chosen template
        Template template = game.getTemplateById(templateId);
        meme.setTemplate(template);

        // add meme to the round
        round.addMeme(meme);

        // update round
        game.setRound(round);

        // perist changes
        save(game);

    }

    public List<Meme> getMemes(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        Round round = game.getRound();

        return round.getMemes();
    }

    /**
     * Submits a rating for a meme by a user
     * 
     * @param gameId
     * @param memeId
     * @param rating
     * @param user
     */
    public void createRating(String gameId, UUID memeId, Rating rating, User user) {
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
     * Sets a player state to ready, meaning they are ready to start the next round
     * 
     * @param gameId
     * @param user
     */
    public void setPlayerReady(String gameId, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<Player> players = game.getPlayers();

        for (Player player : players) {
            if (user.getId() == player.getUser().getId()) {
                player.setState(PlayerState.READY);
            }
        }

        game.setPlayers(players);

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

    /**
     * Executes the job which will manage the state throughout the lifetime of the
     * game
     * 
     * @param gameId
     */
    public void exectue(String gameId) {
        while (true) {
            Game game = getGame(gameId);
            // get game players
            List<Player> players = game.getPlayers();
            // get current round
            Round round = game.getRound();

            // if equal it means everyone submited
            if (round.getSubmitedMemes().size() == players.size()) {
                // close round
                round.setOpen(false);
                // start voting phase
                game.setState(GameState.RATING);

                log.info(gameId + " - Round " + game.getCurrentRound() + " Phase RATING");
            }

            // if equal it means everyone rated
            else if (round.getRatings().size() == players.size()) {
                // start round result phase
                game.setState(GameState.ROUND_RESULTS);

                log.info(gameId + " - Round " + game.getCurrentRound() + " Phase ROUND_RESULTS");
            }

            // check if game is finished
            else if (game.getGameSetting().getMaxRounds() == game.getCurrentRound()) {
                game.setState(GameState.GAME_RESULTS);

                log.info(gameId + " - Round " + game.getCurrentRound() + " Phase GAME_RESULTS");
            }

            // game not finished yet
            else {
                // check if everyone is ready for next round
                boolean allReady = false;
                for (Player player : players) {
                    if (player.getState() == PlayerState.READY) {
                        allReady = true;
                        continue;
                    }
                    allReady = false;
                    break;
                }
                if (allReady) {
                    // start creation phase
                    game.setState(GameState.CREATION);
                    // increment round
                    game.setCurrentRound(game.getCurrentRound() + 1);
                    // initialize new round
                    Round nextRound = game.getRound();
                    nextRound.setOpen(true);
                    nextRound.setRoundNumber(null);
                    nextRound.setStartedAt(LocalDateTime.now());
                    game.setRound(nextRound);

                    // reset player state
                    for (Player player : players) {
                        player.setState(PlayerState.NOT_READY);
                    }
                    game.setPlayers(players);

                    log.info(gameId + " - Round " + game.getCurrentRound() + " Phase CREATION");
                }
            }

            // persist changes
            save(game);

            // sleep for 1 second
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
