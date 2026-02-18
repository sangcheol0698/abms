package kr.co.abacus.abms.domain.notification;

import org.jspecify.annotations.Nullable;

public record NotificationCreateRequest(
        String title,
        @Nullable String description,
        @Nullable NotificationType type,
        @Nullable String link) {

}
