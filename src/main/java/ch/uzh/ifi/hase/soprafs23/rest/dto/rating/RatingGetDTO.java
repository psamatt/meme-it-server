package ch.uzh.ifi.hase.soprafs23.rest.dto.rating;

import ch.uzh.ifi.hase.soprafs23.rest.dto.meme.MemeGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.user.UserGetDTO;

public class RatingGetDTO {

    private UserGetDTO user;

    private MemeGetDTO meme;

    private Integer rating;

    public UserGetDTO getUser() {
        return user;
    }

    public void setUser(UserGetDTO user) {
        this.user = user;
    }

    public MemeGetDTO getMeme() {
        return meme;
    }

    public void setMeme(MemeGetDTO meme) {
        this.meme = meme;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
