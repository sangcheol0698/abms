package kr.co.abacus.abms.adapter.web.chat.dto;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;

import kr.co.abacus.abms.application.chat.dto.command.ChatSendCommand;

public record ChatSendRequest(
        @NotBlank(message = "메시지 내용은 필수입니다.") String content,

        @Nullable String sessionId) {

    public ChatSendCommand toCommand() {
        return new ChatSendCommand(sessionId, content);
    }

}
