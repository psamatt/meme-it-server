package ch.uzh.ifi.hase.soprafs23.repository;

import ch.uzh.ifi.hase.soprafs23.entity.TextBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextBoxRepository extends JpaRepository<TextBox, Long> {
    List<TextBox> findAllByMeme_Id(String uuid);
}
