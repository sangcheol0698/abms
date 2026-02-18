package kr.co.abacus.abms.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    INFO("정보"),
    SUCCESS("성공"),
    WARNING("주의"),
    ERROR("오류");

    private final String description;

}
