package kr.co.abacus.abms.adapter.api.notification.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.notification.Notification;

public record NotificationResponse(
        Long id,
        String title,
        @Nullable String description,
        String type,
        LocalDateTime createdAt,
        boolean read,
        @Nullable String link
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                Objects.requireNonNull(notification.getId()),
                notification.getTitle(),
                notification.getDescription(),
                notification.getType().name(),
                notification.getCreatedAt(),
                notification.isRead(),
                notification.getLink()
        );
    }

}
