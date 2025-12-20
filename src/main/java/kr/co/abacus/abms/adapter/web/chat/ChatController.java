package kr.co.abacus.abms.adapter.web.chat;

import kr.co.abacus.abms.application.chat.ChatService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return chatService.streamMessage(request.content());
    }

    @PostMapping("/message")
    public Map<String, String> sendMessage(@RequestBody ChatRequest request) {
        String content = chatService.sendMessage(request.content());
        return Map.of("content", content);
    }

}
