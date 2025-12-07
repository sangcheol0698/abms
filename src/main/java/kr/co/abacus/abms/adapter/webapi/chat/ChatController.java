package kr.co.abacus.abms.adapter.webapi.chat;

import java.util.Map;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

import kr.co.abacus.abms.application.chat.ChatService;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = "/stream")
    public Flux<String> streamChat(@RequestParam String message) {
        return chatService.streamMessage(message);
    }

    @PostMapping("/message")
    public Map<String, String> sendMessage(@RequestBody ChatRequest request) {
        String content = chatService.sendMessage(request.content());
        return Map.of("content", content);
    }

    @GetMapping("/joke")
    public ChatResponse joke() {
        return chatService.getJoke();
    }
}
