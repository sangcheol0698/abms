package kr.co.abacus.abms.application.chat.dto.query;

import reactor.core.publisher.Flux;

/**
 * Result of streaming chat message.
 * Contains session ID, content stream, and optional tool call events stream.
 */
public record ChatStreamResult(
        String sessionId,
        Flux<String> contentStream,
        Flux<String> toolCallEvents) {

    public ChatStreamResult(String sessionId, Flux<String> contentStream) {
        this(sessionId, contentStream, Flux.empty());
    }

}
