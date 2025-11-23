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
            .user(message)
            .tools(employeeInfoTools)
            .call()
            .content();
    }

}
