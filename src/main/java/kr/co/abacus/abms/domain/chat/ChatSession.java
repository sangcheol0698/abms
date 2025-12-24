package kr.co.abacus.abms.domain.chat;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String sessionId;

    @Column(nullable = false)
    private Boolean favorite = false;

    @OneToMany(mappedBy = "chatSession")
    private List<ChatMessage> messages = new ArrayList<>();

    private ChatSession(String title, String sessionId) {
        this.title = title;
        this.sessionId = sessionId;
        this.favorite = false;
    }

    public static ChatSession create(String title, String sessionId) {
        return new ChatSession(title, sessionId);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }

}
