package kr.co.abacus.abms.domain.notification;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_notification")
public class Notification extends AbstractEntity {

    @Column(name = "notification_title", nullable = false, length = 120)
    private String title;

    @Nullable
    @Column(name = "notification_description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "read", nullable = false)
    private boolean read;

    @Nullable
    @Column(name = "link_url", length = 255)
    private String link;

    public static Notification create(NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.title = requireNonNull(request.title());
        notification.description = request.description();
        notification.type = request.type() != null ? request.type() : NotificationType.INFO;
        notification.link = request.link();
        notification.read = false;
        return notification;
    }

    public void markAsRead() {
        this.read = true;
    }

}
