package kr.co.abacus.abms.application.notification.inbound;

import java.util.List;

import kr.co.abacus.abms.domain.notification.Notification;

public interface NotificationFinder {

    List<Notification> findAll();

}
