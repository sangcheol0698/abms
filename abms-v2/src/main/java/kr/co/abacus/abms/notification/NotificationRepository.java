package kr.co.abacus.abms.notification;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByAccountIdAndReadFalse(Long accountId);

    List<Notification> findAllByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Modifying
    @Query("update Notification n set n.read = true where n.accountId = :accountId and n.read = false")
    int markAllAsRead(Long accountId);

}
