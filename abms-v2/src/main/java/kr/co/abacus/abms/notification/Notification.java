package kr.co.abacus.abms.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 계정별 알림.
 */
@Entity
@Table(name = "tb_notification")
@SQLRestriction("deleted = false")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private @Nullable String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    private @Nullable String link;

    protected Notification() {
    }

    public static Notification create(Long accountId, NotificationType type, String title,
                                      @Nullable String description, @Nullable String link) {
        Notification notification = new Notification();
        notification.accountId = accountId;
        notification.type = type;
        notification.title = title.length() > 120 ? title.substring(0, 120) : title;
        notification.description = description != null && description.length() > 500 ? description.substring(0, 500) : description;
        notification.link = link;
        notification.read = false;
        return notification;
    }

    public void markAsRead() {
        this.read = true;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getTitle() {
        return title;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public @Nullable String getLink() {
        return link;
    }

}
