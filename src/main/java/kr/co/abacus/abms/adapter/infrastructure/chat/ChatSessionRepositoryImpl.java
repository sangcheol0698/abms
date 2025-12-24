package kr.co.abacus.abms.adapter.infrastructure.chat;

import static kr.co.abacus.abms.domain.chat.QChatSession.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;
import kr.co.abacus.abms.application.chat.outbound.CustomChatSessionRepository;

@Repository
@RequiredArgsConstructor
public class ChatSessionRepositoryImpl implements CustomChatSessionRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatSessionSummary> findRecentSessions(int limit) {
        return queryFactory
                .select(Projections.constructor(ChatSessionSummary.class,
                        chatSession.id,
                        chatSession.sessionId,
                        chatSession.title,
                        chatSession.favorite,
                        chatSession.updatedAt))
                .from(chatSession)
                .where(chatSession.deleted.isFalse())
                .orderBy(chatSession.updatedAt.desc())
                .limit(limit)
                .fetch();
    }

}
