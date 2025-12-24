package kr.co.abacus.abms.domain.chat;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends AbstractEntity {

    public enum Role {
        USER,
        ASSISTANT
    }

    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private ChatMessage(Role role, String content, ChatSession chatSession) {
        this.role = role;
        this.content = content;
        this.chatSession = chatSession;
        this.timestamp = LocalDateTime.now();
    }

    public static ChatMessage createUserMessage(String content, ChatSession chatSession) {
        return new ChatMessage(Role.USER, content, chatSession);
    }

    public static ChatMessage createAssistantMessage(String content, ChatSession chatSession) {
        return new ChatMessage(Role.ASSISTANT, content, chatSession);
    }

    public void appendContent(String chunk) {
        this.content = this.content + chunk;
    }

}
