package kr.co.abacus.abms.assistant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 화면에 표시되는 대화 메시지 (LLM 메모리와 별도로 전체 이력을 보관한다).
 */
@Entity
@Table(name = "tb_chat_message")
public class ChatMessage extends BaseEntity {

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ChatMessage() {
    }

    public static ChatMessage of(Long sessionId, Role role, String content) {
        ChatMessage message = new ChatMessage();
        message.sessionId = sessionId;
        message.role = role;
        message.content = content;
        return message;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return role == Role.USER;
    }

    public enum Role {
        USER, ASSISTANT
    }

}
