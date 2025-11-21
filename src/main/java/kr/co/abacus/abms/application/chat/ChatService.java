package kr.co.abacus.abms.application.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final EmployeeInfoTools employeeInfoTools;

    public ChatService(ChatClient.Builder chatClientBuilder, EmployeeInfoTools employeeInfoTools) {
        this.chatClient = chatClientBuilder.build();
        this.employeeInfoTools = employeeInfoTools;
    }

    public Flux<String> streamMessage(String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content();
    }

    public String sendMessage(String message) {
        return chatClient.prompt()
            .system("""
                    당신은 HR 보조원입니다. 제공된 직원 정보 도구를 사용하여 직원에 관한 질문에 답하세요.
                    정보를 사용할 수 없는 경우 "해당 정보가 없습니다."라고 응답하세요.
                """)
            .user(message)
            .tools(employeeInfoTools)
            .call()
            .content();
    }

}
