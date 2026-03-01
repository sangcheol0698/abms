package kr.co.abacus.abms.adapter.infrastructure.chat;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.chat.ChatMessage;

public interface ChatMessageRepository
        extends Repository<ChatMessage, Long>,
        kr.co.abacus.abms.application.chat.outbound.ChatMessageRepository {
}
