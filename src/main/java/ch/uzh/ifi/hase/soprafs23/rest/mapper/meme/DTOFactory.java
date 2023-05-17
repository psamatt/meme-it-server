package ch.uzh.ifi.hase.soprafs23.rest.mapper.meme;

import ch.uzh.ifi.hase.soprafs23.entity.Meme;
import ch.uzh.ifi.hase.soprafs23.entity.TextBox;
import ch.uzh.ifi.hase.soprafs23.rest.dto.meme.MemeGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.meme.MemePostDTO;
import org.mapstruct.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class DTOFactory implements MemeMapper{
    @ObjectFactory
    public MemeGetDTO convertEntityToMemeGetDTO(Meme meme) {
        MemeGetDTO memeGetDTO = new MemeGetDTO();

        memeGetDTO.setId(meme.getId());
        memeGetDTO.setImageUrl(meme.getTemplate().getImageUrl());
        memeGetDTO.setColor(meme.getColor());
        memeGetDTO.setFontSize(meme.getFontSize());
        memeGetDTO.setUser(meme.getUser());
        memeGetDTO.setBackgroundColor(meme.getBackgroundColor());
        List<TextBox> textBoxes = new ArrayList<>();
        for (TextBox t : meme.getTextBoxes()) {
            TextBox textBox = new TextBox();
            textBox.setText(t.getText());
            textBox.setxRate(t.getxRate());
            textBox.setyRate(t.getyRate());
            textBoxes.add(textBox);
        }
        memeGetDTO.setTextBoxes(textBoxes);

        return memeGetDTO;
    }

    @ObjectFactory
    public Meme convertMemePostDTOtoEntity(MemePostDTO memePostDTO){
        Meme m = new Meme();
        m.setColor(memePostDTO.getColor());
        m.setFontSize(memePostDTO.getFontSize());
        m.setBackgroundColor(memePostDTO.getBackgroundColor());
        ArrayList<TextBox> tb = new ArrayList<>();
        for (TextBox t : memePostDTO.getTextBoxes()) {
            TextBox textBox = new TextBox();
            textBox.setText(t.getText());
            textBox.setxRate(t.getxRate());
            textBox.setyRate(t.getyRate());
            textBox.setMeme(m); // Set the meme reference in TextBox
            tb.add(textBox);
        }
        m.setTextBoxes(tb);
        return m;
    }
}
