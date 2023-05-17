package ch.uzh.ifi.hase.soprafs23.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs23.entity.Game;

@Repository("gameRepository")
public interface GameRepository extends JpaRepository<Game, Long> {
    @Query("SELECT g FROM Game g JOIN FETCH g.rounds WHERE g.id = :gameId")
    Optional<Game> findByIdWithRounds(@Param("gameId") String gameId);

    Optional<Game> findById(String id);
}
