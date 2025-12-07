package kr.co.abacus.abms.application.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import kr.co.abacus.abms.application.chat.provided.tools.EmployeeInfoTools;
import kr.co.abacus.abms.application.chat.provided.tools.EmployeeSearchTools;
import kr.co.abacus.abms.application.chat.provided.tools.OrganizationTools;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final EmployeeInfoTools employeeInfoTools;
    private final OrganizationTools organizationTools;
    private final EmployeeSearchTools employeeSearchTools;

    public ChatService(
        ChatClient.Builder chatClientBuilder,
        EmployeeInfoTools employeeInfoTools,
        OrganizationTools organizationTools,
        EmployeeSearchTools employeeSearchTools
    ) {
        this.chatClient = chatClientBuilder.build();
        this.employeeInfoTools = employeeInfoTools;
        this.organizationTools = organizationTools;
        this.employeeSearchTools = employeeSearchTools;
    }

    public String sendMessage(String message) {
        String currentDateTime = java.time.LocalDateTime.now().toString();

        return chatClient.prompt()
            .system("""
                당신은 유능한 비서입니다. 현재 날짜와 시간은 %s 입니다.
                """.formatted(currentDateTime))
            .user(message)
            // .tools(employeeInfoTools, organizationTools, employeeSearchTools)
            .call()
            .content();
    }

    public Flux<String> streamMessage(String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content();
    }

    public ChatResponse getJoke() {
        return chatClient.prompt()
            .system("당신은 유머러스한 조크 생성기입니다.")
            .user("재미있는 농담 하나 해줘.")
            .call()
            .chatResponse();
    }

}
