package ch.uzh.ifi.hase.soprafs23.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "TEXTBOX")
public class TextBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private Integer xRate;
    private Integer yRate;
    @ManyToOne
    @JsonIgnore
    private Meme meme;

    public TextBox(){}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getxRate() {
        return xRate;
    }

    public void setxRate(Integer xRate) {
        this.xRate = xRate;
    }

    public Integer getyRate() {
        return yRate;
    }

    public void setyRate(Integer yRate) {
        this.yRate = yRate;
    }

    public Meme getMeme() {
        return meme;
    }

    public void setMeme(Meme meme) {
        this.meme = meme;
    }
}
