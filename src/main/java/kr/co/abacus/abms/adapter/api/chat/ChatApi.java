package kr.co.abacus.abms.adapter.api.chat;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import kr.co.abacus.abms.adapter.api.chat.dto.ChatMessageResponse;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatSendRequest;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatSessionResponse;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatStreamChunk;
import kr.co.abacus.abms.application.chat.ChatCommandService;
import kr.co.abacus.abms.application.chat.ChatQueryService;
import kr.co.abacus.abms.application.chat.dto.command.ChatSendCommand;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatApi {

    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamChunk> streamChat(@RequestBody @Valid ChatSendRequest request) {
        ChatSendCommand command = request.toCommand();
        return chatCommandService.streamMessage(command)
                .flatMapMany(result -> {
                    // Session info first
                    Flux<ChatStreamChunk> sessionInfo = Flux.just(ChatStreamChunk.session(result.sessionId()));

                    // Tool call events
                    Flux<ChatStreamChunk> toolCalls = result.toolCallEvents().map(ChatStreamChunk::toolCall);

                    // Content chunks
                    Flux<ChatStreamChunk> contentChunks = result.contentStream().map(ChatStreamChunk::text);

                    // Merge tool calls with content (tool calls come through as they happen)
                    return sessionInfo.concatWith(toolCalls.mergeWith(contentChunks));
                });
    }

    @PostMapping("/message")
    public ChatMessageResponse sendMessage(@RequestBody @Valid ChatSendRequest request) {
        ChatSendCommand command = request.toCommand();
        String content = chatCommandService.sendMessage(command);
        return ChatMessageResponse.assistantResponse(content);
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> getRecentSessions(
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatSessionSummary> sessions = chatQueryService.getRecentSessions(limit);
        return sessions.stream()
                .map(ChatSessionResponse::from)
                .toList();
    }

    @GetMapping("/sessions/favorites")
    public List<ChatSessionResponse> getFavoriteSessions() {
        List<ChatSessionSummary> sessions = chatQueryService.getFavoriteSessions();
        return sessions.stream()
                .map(ChatSessionResponse::from)
                .toList();
    }

    @GetMapping("/sessions/{sessionId}")
    public ChatSessionResponse getSessionDetail(@PathVariable String sessionId) {
        ChatSessionDetail detail = chatQueryService.getSessionDetail(sessionId);
        return ChatSessionResponse.from(detail);
    }

    @PostMapping("/sessions/{sessionId}/favorite")
    public void toggleFavorite(@PathVariable String sessionId) {
        chatCommandService.toggleFavorite(sessionId);
    }

}
