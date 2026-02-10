package kr.co.abacus.abms.domain.project;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RevenueType {
    DOWN_PAYMENT("착수금"),
    INTERMEDIATE_PAYMENT("중도금"),
    BALANCE_PAYMENT("잔금"),
    MAINTENANCE("유지보수"),
    ETC("기타");

    private final String description;

    public static RevenueType fromDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("프로젝트 매출 유형을 입력하세요.");
        }

        String normalized = description.trim();
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(normalized)
                        || status.description.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 프로젝트 매출 유형: " + description));
    }
}
