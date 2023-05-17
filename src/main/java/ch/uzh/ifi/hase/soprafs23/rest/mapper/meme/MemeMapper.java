package ch.uzh.ifi.hase.soprafs23.rest.mapper.meme;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs23.entity.Meme;
import ch.uzh.ifi.hase.soprafs23.rest.dto.meme.MemeGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.meme.MemePostDTO;

@Mapper(uses = { DTOFactory.class })
public interface MemeMapper {
    MemeMapper INSTANCE = Mappers.getMapper(MemeMapper.class);

    Meme convertMemePostDTOtoEntity(MemePostDTO memePostDTO);

    MemeGetDTO convertEntityToMemeGetDTO(Meme meme);

}
