package kr.co.abacus.abms.application.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.notification.inbound.NotificationFinder;
import kr.co.abacus.abms.application.notification.outbound.NotificationRepository;
import kr.co.abacus.abms.domain.notification.Notification;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationQueryService implements NotificationFinder {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
    }

}
