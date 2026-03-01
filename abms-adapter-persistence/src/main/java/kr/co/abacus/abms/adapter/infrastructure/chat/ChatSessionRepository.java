package kr.co.abacus.abms.adapter.infrastructure.chat;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.chat.ChatSession;

public interface ChatSessionRepository
        extends Repository<ChatSession, Long>,
        kr.co.abacus.abms.application.chat.outbound.ChatSessionRepository {
}
