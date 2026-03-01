package kr.co.abacus.abms.adapter.infrastructure.notification;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.notification.Notification;

public interface NotificationRepository
        extends Repository<Notification, Long>,
        kr.co.abacus.abms.application.notification.outbound.NotificationRepository {
}
