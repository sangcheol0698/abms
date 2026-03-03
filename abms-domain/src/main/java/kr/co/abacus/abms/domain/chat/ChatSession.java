package kr.co.abacus.abms.domain.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_chat_session", uniqueConstraints = {
        @UniqueConstraint(name = "UK_CHAT_SESSION_ACCOUNT_SESSION_ID", columnNames = {"account_id", "session_id"})
})
public class ChatSession extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Boolean favorite = false;

    @OneToMany(mappedBy = "chatSession")
    private List<ChatMessage> messages = new ArrayList<>();

    private ChatSession(String title, String sessionId, Long accountId) {
        this.title = Objects.requireNonNull(title);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.accountId = Objects.requireNonNull(accountId);
        this.favorite = false;
    }

    public static ChatSession create(String title, String sessionId, Long accountId) {
        return new ChatSession(title, sessionId, accountId);
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
