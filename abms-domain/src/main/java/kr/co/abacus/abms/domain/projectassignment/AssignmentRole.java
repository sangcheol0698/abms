package kr.co.abacus.abms.domain.projectassignment;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssignmentRole {
    DEV("개발자"),
    PM("PM"),
    PL("PL"),
    ETC("기타");

    private final String description;

    public static AssignmentRole fromDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("프로젝트 투입입력 역할을 입력하세요.");
        }

        String normalized = description.trim();
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(normalized)
                        || status.description.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 역할: " + description));
    }
}
