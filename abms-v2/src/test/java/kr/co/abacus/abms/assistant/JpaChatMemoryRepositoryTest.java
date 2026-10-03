package kr.co.abacus.abms.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class JpaChatMemoryRepositoryTest {

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private JpaChatMemoryRepository repository;

    @Test
    void 대화_메모리를_순서대로_저장하고_불러온다() {
        chatMemory.add("conv-1", List.of(new UserMessage("안녕"), new AssistantMessage("안녕하세요")));
        chatMemory.add("conv-1", new UserMessage("프로젝트 알려줘"));

        List<Message> messages = repository.findByConversationId("conv-1");
        assertThat(messages).extracting(Message::getMessageType)
                .containsExactly(MessageType.USER, MessageType.ASSISTANT, MessageType.USER);
        assertThat(messages).extracting(Message::getText).containsExactly("안녕", "안녕하세요", "프로젝트 알려줘");
        assertThat(repository.findConversationIds()).contains("conv-1");

        chatMemory.clear("conv-1");
        assertThat(repository.findByConversationId("conv-1")).isEmpty();
    }

}
