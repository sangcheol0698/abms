package kr.co.abacus.abms.application.notification.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.notification.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndAccountIdAndDeletedFalse(Long id, Long accountId);

    List<Notification> findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(Long accountId);

}
