package kr.co.abacus.abms.assistant;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * AI 어시스턴트 대화 세션.
 */
@Entity
@Table(name = "tb_chat_session")
@SQLRestriction("deleted = false")
public class ChatSession extends BaseEntity {

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, unique = true, length = 64)
    private String conversationId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private boolean favorite;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;

    protected ChatSession() {
    }

    public static ChatSession start(Long accountId, String firstMessage) {
        ChatSession session = new ChatSession();
        session.accountId = accountId;
        session.conversationId = UUID.randomUUID().toString();
        session.title = summarize(firstMessage);
        session.favorite = false;
        session.lastMessageAt = LocalDateTime.now();
        return session;
    }

    public void rename(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("제목을 입력하세요.");
        }
        this.title = summarize(title);
    }

    public void touch() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void toggleFavorite() {
        this.favorite = !favorite;
    }

    private static String summarize(String text) {
        String oneLine = text.strip().replaceAll("\\s+", " ");
        return oneLine.length() > 40 ? oneLine.substring(0, 40) + "…" : oneLine;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

}
