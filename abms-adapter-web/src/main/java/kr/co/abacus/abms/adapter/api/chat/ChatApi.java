package kr.co.abacus.abms.adapter.api.chat;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import kr.co.abacus.abms.adapter.api.chat.dto.ChatMessageResponse;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatSendRequest;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatSessionResponse;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatSessionTitleUpdateRequest;
import kr.co.abacus.abms.adapter.api.chat.dto.ChatStreamChunk;
import kr.co.abacus.abms.application.chat.ChatCommandService;
import kr.co.abacus.abms.application.chat.ChatQueryService;
import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.chat.dto.command.ChatSendCommand;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatApi {

    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;
    private final AuthFinder authFinder;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamChunk> streamChat(
            @RequestBody @Valid ChatSendRequest request,
            Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        ChatSendCommand command = request.toCommand();
        return chatCommandService.streamMessage(accountId, command)
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
    public ChatMessageResponse sendMessage(
            @RequestBody @Valid ChatSendRequest request,
            Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        ChatSendCommand command = request.toCommand();
        String content = chatCommandService.sendMessage(accountId, command);
        return ChatMessageResponse.assistantResponse(content);
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> getRecentSessions(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        List<ChatSessionSummary> sessions = chatQueryService.getRecentSessions(accountId, limit);
        return sessions.stream()
                .map(ChatSessionResponse::from)
                .toList();
    }

    @GetMapping("/sessions/favorites")
    public List<ChatSessionResponse> getFavoriteSessions(Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        List<ChatSessionSummary> sessions = chatQueryService.getFavoriteSessions(accountId);
        return sessions.stream()
                .map(ChatSessionResponse::from)
                .toList();
    }

    @GetMapping("/sessions/{sessionId}")
    public ChatSessionResponse getSessionDetail(@PathVariable String sessionId, Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        ChatSessionDetail detail = chatQueryService.getSessionDetail(accountId, sessionId);
        return ChatSessionResponse.from(detail);
    }

    @PostMapping("/sessions/{sessionId}/favorite")
    public void toggleFavorite(@PathVariable String sessionId, Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        chatCommandService.toggleFavorite(accountId, sessionId);
    }

    @PatchMapping("/sessions/{sessionId}/title")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSessionTitle(
            @PathVariable String sessionId,
            @RequestBody @Valid ChatSessionTitleUpdateRequest request,
            Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        chatCommandService.updateSessionTitle(accountId, sessionId, request.title());
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable String sessionId, Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        chatCommandService.deleteSession(accountId, sessionId);
    }

    private Long resolveAccountId(Authentication authentication) {
        return authFinder.getCurrentAccountId(authentication.getName());
    }

}
