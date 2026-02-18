package kr.co.abacus.abms.adapter.api.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatSessionTitleUpdateRequest(
        @NotBlank(message = "세션 제목은 필수입니다.")
        @Size(max = 200, message = "세션 제목은 200자를 초과할 수 없습니다.")
        String title) {
}
