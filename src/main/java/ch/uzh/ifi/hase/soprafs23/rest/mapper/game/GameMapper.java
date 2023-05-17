package ch.uzh.ifi.hase.soprafs23.rest.mapper.game;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs23.entity.Game;
import ch.uzh.ifi.hase.soprafs23.rest.dto.game.GameGetDTO;

@Mapper(uses = { DTOFactory.class })
public interface GameMapper {

    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    // Mapping taken care of by DTO factory for readability
    GameGetDTO convertEntityToGameGetDTO(Game game);

}