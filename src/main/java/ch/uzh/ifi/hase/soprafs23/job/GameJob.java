package ch.uzh.ifi.hase.soprafs23.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs23.entity.Game;
import ch.uzh.ifi.hase.soprafs23.entity.GameState;
import ch.uzh.ifi.hase.soprafs23.entity.Meme;
import ch.uzh.ifi.hase.soprafs23.entity.Rating;
import ch.uzh.ifi.hase.soprafs23.entity.Round;
import ch.uzh.ifi.hase.soprafs23.entity.User;

// TODO: make testable by seperating components into smaller functions 

/**
 * Contains the code for the acutal game server
 * 
 * Game server updates every second
 */
@Service

// @Transactional
// ! this wraps the method in a transaction and only commits that transaction
// ! once method exists. Thus it cant be used in a while (true) loop for the job
public class GameJob {

    private final Logger log = LoggerFactory.getLogger(GameJob.class);

    @Autowired
    private SessionFactory sessionFactory;

    public GameJob() {
    }

    /**
     * Executes the job which will manage the state throughout the lifetime of the
     * game
     * 
     * @param gameId
     * @throws IllegalArgumentException if game is not found or certain attributes
     *                                  are null
     */
    public void run(String gameId) throws IllegalArgumentException {
        Session session = sessionFactory.openSession();

        boolean gameRunning = true;
        while (gameRunning) {
            // start transaction
            Transaction transaction = session.beginTransaction();

            // get game
            Game game = (Game) session.get(Game.class, gameId);
            if (game == null || game.getId() == null || game.getId().isEmpty()) {
                throw new IllegalArgumentException("Game not found");
            }

            // get game players
            List<User> players = game.getPlayers();
            // get current round
            Round round = game.getRound();

            // Calculate different phase start times
            Long roundStart = round.getStartedAt().getTime();
            Long ratingStart = roundStart + (game.getGameSetting().getRoundDuration() * 1000);
            Long roundResultsStart = ratingStart + (game.getGameSetting().getRatingDuration() * 1000);
            Long nextRoundStart = roundResultsStart + (game.getGameSetting().getRoundResultDuration() * 1000);

            Long timeNow = Calendar.getInstance().getTime().getTime();

            // System.out.println("Game id: " + game.getId());
            // System.out.println("Game state: " + game.getState());
            // System.out.println("Game round number: " + game.getCurrentRound());
            // System.out.println("\n\n");

            // Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            // System.out.println("\nRound started at: " + format.format(new
            // Date(roundStart)));
            // System.out.println("Rating started at: " + format.format(new
            // Date(ratingStart)));
            // System.out.println("Round results started at: " + format.format(new
            // Date(roundResultsStart)));
            // System.out.println("Next round started at: " + format.format(new
            // Date(nextRoundStart)));
            // System.out.println("\n");

            // check if game is finished
            if (game.getState() == GameState.RATING
                    && game.getGameSetting().getMaxRounds().equals(game.getCurrentRound())
                    && roundResultsStart <= timeNow) {

                // set game state
                game.setState(GameState.GAME_RESULTS);

                // save changes
                session.save(game);

                gameRunning = false;

                System.out.println("GameId: " + gameId + " - Round " + game.getCurrentRound() + " Phase GAME_RESULTS");

            }
            // game not finished yet, next round
            // check if round_result phase is over
            else if (game.getState() == GameState.ROUND_RESULTS
                    && nextRoundStart <= timeNow) {

                // start creation phase
                game.setState(GameState.CREATION);
                // increment round
                game.setCurrentRound(game.getCurrentRound() + 1);
                // initialize new round
                Round nextRound = new Round();
                List<Meme> memes = new ArrayList<Meme>(players.size());
                List<Rating> ratings = new ArrayList<Rating>(players.size() * (players.size() - 1));
                nextRound.setMemes(memes);
                nextRound.setRatings(ratings);
                nextRound.setOpen(true);
                nextRound.setRoundNumber(round.getRoundNumber() + 1);
                nextRound.setStartedAt(Calendar.getInstance().getTime());
                game.addRound(nextRound);

                // persist changes
                session.save(game);

                System.out.println("GameId: " + gameId + " - Round " + game.getCurrentRound() + " Phase CREATION");
            }
            // check if everyone rated or rating phase is over
            else if (round.getRatings().size() == players.size() || game.getState() == GameState.RATING
                    && roundResultsStart <= timeNow) {
                // start round result phase
                game.setState(GameState.ROUND_RESULTS);

                // save changes
                session.save(game);

                System.out.println("GameId: " + gameId + " - Round " + game.getCurrentRound() + " Phase ROUND_RESULTS");

            }
            // check if everyone submited or creation phase is over
            else if (round.getSubmitedMemes().size() == players.size() || game.getState() == GameState.CREATION
                    && ratingStart <= timeNow) {
                // close round
                round.setOpen(false);
                // start voting phase
                game.setState(GameState.RATING);

                // save changes
                session.save(game);

                System.out.println("GameId: " + gameId + " - Round " + game.getCurrentRound() + " Phase RATING");
            }

            // persist changes by commiting transaction
            transaction.commit();

            // sleep for 1 second
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException ie) {
                log.error("GameJob was interrupted", ie);
                Thread.currentThread().interrupt();
            }
        }

        // delete the game after 10 seconds
        // (allows client more than enough time to get state)
        try {
            Thread.sleep(10_000);
            // start transaction
            Transaction transaction = session.beginTransaction();
            // delete the game
            Game game = (Game) session.get(Game.class, gameId);
            if (game == null || game.getId() == null || game.getId().isEmpty()) {
                throw new IllegalArgumentException("Game not found");
            }
            session.delete(game);
            transaction.commit();
        } catch (InterruptedException ie) {
            log.error("Unable to delete game after delay", ie);
            Thread.currentThread().interrupt();
        }

        // close session
        session.close();

    }

}
