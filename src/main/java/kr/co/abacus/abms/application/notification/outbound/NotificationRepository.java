package kr.co.abacus.abms.application.notification.outbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.notification.Notification;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndDeletedFalse(Long id);

    List<Notification> findAllByDeletedFalseOrderByCreatedAtDesc();

}
