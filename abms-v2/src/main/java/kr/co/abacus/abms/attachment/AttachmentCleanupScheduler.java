package kr.co.abacus.abms.attachment;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 보관 기간(기본 30일)이 지난 삭제 첨부를 정리한다. (abms.attachment.cleanup-enabled=false 로 끌 수 있다)
 */
@Component
public class AttachmentCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttachmentCleanupScheduler.class);

    private final AttachmentCleanupService cleanupService;
    private final boolean enabled;
    private final int retentionDays;

    public AttachmentCleanupScheduler(AttachmentCleanupService cleanupService,
                                      @Value("${abms.attachment.cleanup-enabled:true}") boolean enabled,
                                      @Value("${abms.attachment.retention-days:30}") int retentionDays) {
        this.cleanupService = cleanupService;
        this.enabled = enabled;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${abms.attachment.cleanup-cron:0 30 3 * * *}", zone = "Asia/Seoul")
    public void cleanup() {
        if (!enabled) {
            return;
        }
        try {
            int purged = cleanupService.purge(LocalDateTime.now().minusDays(retentionDays));
            if (purged > 0) {
                log.info("삭제한 첨부 파일 {}건 정리", purged);
            }
        } catch (RuntimeException e) {
            log.error("첨부 파일 정리 실패", e);
        }
    }

}
