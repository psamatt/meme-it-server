package ch.uzh.ifi.hase.soprafs23.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

// * NOTE: using entity & table instead of embeddable did not solve the game state not updating
@Embeddable
public class GameSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private Integer maxRounds;

    @Column(nullable = false)
    private Integer templateSwapLimit;
    // private Integer superLikeLimit;
    // private Integer superDislikeLimit;

    @Column(nullable = false)
    private Integer roundDuration;

    @Column(nullable = false)
    private Integer ratingDuration;

    @Column(nullable = false)
    private Integer roundResultDuration;

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Integer getTemplateSwapLimit() {
        return templateSwapLimit;
    }

    public void setTemplateSwapLimit(Integer memeChangeLimit) {
        this.templateSwapLimit = memeChangeLimit;
    }

    // public Integer getSuperLikeLimit() {
    // return superLikeLimit;
    // }

    // public void setSuperLikeLimit(Integer superLikeLimit) {
    // this.superLikeLimit = superLikeLimit;
    // }

    // public Integer getSuperDislikeLimit() {
    // return superDislikeLimit;
    // }

    // public void setSuperDislikeLimit(Integer superDislikeLimit) {
    // this.superDislikeLimit = superDislikeLimit;
    // }

    public Integer getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(Integer roundDuration) {
        this.roundDuration = roundDuration;
    }

    public Integer getRatingDuration() {
        return ratingDuration;
    }

    public void setRatingDuration(Integer ratingDuration) {
        this.ratingDuration = ratingDuration;
    }

    public Integer getRoundResultDuration() {
        return roundResultDuration;
    }

    public void setRoundResultDuration(Integer roundResultDuration) {
        this.roundResultDuration = roundResultDuration;
    }
}
