package kr.co.abacus.abms.adapter.web.chat.dto;

import org.jspecify.annotations.Nullable;

public record ChatStreamChunk(
        @Nullable String text,
        @Nullable String sessionId,
        @Nullable String toolName,
        @Nullable String type) {

    public static ChatStreamChunk text(String text) {
        return new ChatStreamChunk(text, null, null, "text");
    }

    public static ChatStreamChunk session(String sessionId) {
        return new ChatStreamChunk(null, sessionId, null, "session");
    }

    public static ChatStreamChunk toolCall(String toolName) {
        return new ChatStreamChunk(null, null, toolName, "tool_call");
    }

}
