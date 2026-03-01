package kr.co.abacus.abms.adapter.api.notification.dto;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;
import kr.co.abacus.abms.domain.notification.NotificationType;

public record NotificationCreateApiRequest(
        String title,
        @Nullable String description,
        @Nullable NotificationType type,
        @Nullable String link
) {

    public NotificationCreateRequest toDomainRequest() {
        return new NotificationCreateRequest(
                title,
                description,
                type,
                link
        );
    }

}
