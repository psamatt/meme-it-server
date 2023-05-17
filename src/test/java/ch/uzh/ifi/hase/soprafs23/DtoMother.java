package ch.uzh.ifi.hase.soprafs23;

import ch.uzh.ifi.hase.soprafs23.rest.dto.meme.MemePostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.rating.RatingPostDTO;

import java.util.List;

public class DtoMother {

    public static RatingPostDTO buildRatingPostDTO() {
        RatingPostDTO dto = new RatingPostDTO();
        dto.setRating(1);
        return dto;
    }

    public static MemePostDTO buildMemePostDTO() {
        MemePostDTO dto = new MemePostDTO();
        dto.setColor("color");
        dto.setCurrentRound(1);
        dto.setFontSize(2);
        dto.setTextBoxes(List.of(EntityMother.defaulTextBox()));
        return dto;
    }
}
