package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.entity.Message;
import ch.uzh.ifi.hase.soprafs23.repository.ChatRepository;
import ch.uzh.ifi.hase.soprafs23.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.chat.ChatPostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    private final UserRepository userRepository;
    private final LobbyRepository lobbyRepository;
    private final ChatRepository chatRepository;

    public ChatService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository,
            @Qualifier("chatRepository") ChatRepository chatRepository,
            @Qualifier("userRepository") UserRepository userRepository) {
        this.lobbyRepository = lobbyRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public List<Message> getProximityChatMessages(String lobbyCode) {
        return lobbyRepository.findByCode(lobbyCode).getMessages();
    }

    public List<Message> writeNewProximityChatMessage(String lobbyCode, ChatPostDTO chatPostDTO) {
        Message m = new Message();
        m.setMessage(chatPostDTO.getMessage());
        m.setCreatedAt(LocalDateTime.now());
        m.setUser(userRepository.findById(chatPostDTO.getAuthor()));

        chatRepository.save(m);
        chatRepository.flush();

        Lobby l = lobbyRepository.findByCode(lobbyCode);
        l.addMessage(m);
        lobbyRepository.save(l);
        lobbyRepository.flush();

        log.debug("New chat message written and added to lobby", l);
        return l.getMessages();
    }
}