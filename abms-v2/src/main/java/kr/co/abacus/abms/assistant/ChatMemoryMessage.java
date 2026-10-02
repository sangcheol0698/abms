package kr.co.abacus.abms.assistant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

/**
 * LLM 대화 메모리(최근 N개 메시지 창). Spring AI ChatMemoryRepository 저장소.
 */
@Entity
@Table(name = "tb_chat_memory_message")
public class ChatMemoryMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(nullable = false, length = 64)
    private String conversationId;

    @Column(nullable = false)
    private int seq;

    @Column(nullable = false, length = 20)
    private String messageType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ChatMemoryMessage() {
    }

    public ChatMemoryMessage(String conversationId, int seq, String messageType, String content) {
        this.conversationId = conversationId;
        this.seq = seq;
        this.messageType = messageType;
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public int getSeq() {
        return seq;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

}
