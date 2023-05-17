package ch.uzh.ifi.hase.soprafs23.rest.mapper.game;

import ch.uzh.ifi.hase.soprafs23.entity.Game;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.game.GameGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.user.UserMapper;

import org.mapstruct.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class DTOFactory {

    @ObjectFactory
    public GameGetDTO convertEntityToGameGetDTO(Game game) {

        GameGetDTO gameGetDTO = new GameGetDTO();

        gameGetDTO.setId(game.getId());
        gameGetDTO.setGameState(game.getState());
        gameGetDTO.setRoundDuration(game.getGameSetting().getRoundDuration());
        gameGetDTO.setVotingDuration(game.getGameSetting().getRatingDuration());
        gameGetDTO.setRoundResultDuration(game.getGameSetting().getRoundResultDuration());
        gameGetDTO.setTotalRounds(game.getGameSetting().getMaxRounds());
        gameGetDTO.setCurrentRound(game.getCurrentRound());
        gameGetDTO.setRoundStartedAt(game.getRound().getStartedAt());
        gameGetDTO.setStartedAt(game.getStartedAt());

        List<UserGetDTO> playerDTOs = new ArrayList<>();
        for (User player : game.getPlayers()) {
            playerDTOs.add(UserMapper.INSTANCE.convertEntityToUserGetDTO(player));
        }
        gameGetDTO.setPlayers(playerDTOs);

        return gameGetDTO;
    }

}