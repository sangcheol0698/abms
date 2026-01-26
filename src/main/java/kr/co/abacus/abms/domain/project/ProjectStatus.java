package kr.co.abacus.abms.domain.project;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectStatus {
    SCHEDULED("예약"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    ON_HOLD("보류"),
    CANCELLED("취소");

    private final String description;

    public static ProjectStatus fromDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("프로젝트 상태를 입력하세요.");
        }

        String normalized = description.trim();
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(normalized)
                        || status.description.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 프로젝트 상태: " + description));
    }
}
