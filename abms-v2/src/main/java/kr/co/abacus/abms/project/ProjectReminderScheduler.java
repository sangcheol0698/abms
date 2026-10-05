package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 아침 청구·종료 알림을 보낸다. (abms.reminder.enabled=false 로 끌 수 있다)
 */
@Component
public class ProjectReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProjectReminderScheduler.class);

    private final ProjectReminderService reminderService;
    private final boolean enabled;

    public ProjectReminderScheduler(ProjectReminderService reminderService, @Value("${abms.reminder.enabled:true}") boolean enabled) {
        this.reminderService = reminderService;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${abms.reminder.cron:0 30 8 * * *}", zone = "Asia/Seoul")
    public void remind() {
        if (!enabled) {
            return;
        }
        try {
            int sent = reminderService.run(LocalDate.now(ZoneId.of("Asia/Seoul")));
            log.info("정기 알림 {}건 발송", sent);
        } catch (RuntimeException e) {
            log.error("정기 알림 실패", e);
        }
    }

}
