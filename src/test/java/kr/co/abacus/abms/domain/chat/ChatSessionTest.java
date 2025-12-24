package kr.co.abacus.abms.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatSessionTest {

    @Test
    @DisplayName("채팅 세션 생성")
    void create() {
        String sessionId = "test-session-id";
        ChatSession session = ChatSession.create("테스트 대화", sessionId);

        assertThat(session.getTitle()).isEqualTo("테스트 대화");
        assertThat(session.getSessionId()).isEqualTo(sessionId);
        assertThat(session.getFavorite()).isFalse();
        assertThat(session.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("채팅 세션 제목 수정")
    void updateTitle() {
        ChatSession session = ChatSession.create("원래 제목", "session-id");
        session.updateTitle("새 제목");

        assertThat(session.getTitle()).isEqualTo("새 제목");
    }

    @Test
    @DisplayName("즐겨찾기 토글")
    void toggleFavorite() {
        ChatSession session = ChatSession.create("테스트 대화", "session-id");

        assertThat(session.getFavorite()).isFalse();

        session.toggleFavorite();
        assertThat(session.getFavorite()).isTrue();

        session.toggleFavorite();
        assertThat(session.getFavorite()).isFalse();
    }

    @Test
    @DisplayName("메시지 추가")
    void addMessage() {
        ChatSession session = ChatSession.create("테스트 대화", "session-id");
        ChatMessage message = ChatMessage.createUserMessage("안녕하세요", session);

        assertThat(session.getMessages()).isEmpty();

        session.addMessage(message);

        assertThat(session.getMessages()).hasSize(1);
        assertThat(session.getMessages().get(0)).isEqualTo(message);
    }

}
