package kr.co.abacus.abms.notice;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

/**
 * 사용자별 공지 수신 상태: 읽음, 안내 팝업 숨김(오늘 하루 / 다시 보지 않기).
 */
@Entity
@Table(name = "tb_notice_receipt")
public class NoticeReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(nullable = false)
    private Long noticeId;

    @Column(nullable = false)
    private Long accountId;

    private @Nullable LocalDateTime readAt;

    private @Nullable LocalDateTime popupHiddenUntil;

    @Column(nullable = false)
    private boolean popupHiddenForever;

    protected NoticeReceipt() {
    }

    public static NoticeReceipt of(Long noticeId, Long accountId) {
        NoticeReceipt receipt = new NoticeReceipt();
        receipt.noticeId = noticeId;
        receipt.accountId = accountId;
        return receipt;
    }

    public void markRead(LocalDateTime now) {
        if (readAt == null) {
            readAt = now;
        }
    }

    public void hidePopupUntil(LocalDateTime until) {
        this.popupHiddenUntil = until;
    }

    public void hidePopupForever() {
        this.popupHiddenForever = true;
    }

    /** 지금 팝업을 숨겨야 하는지 */
    public boolean popupHiddenAt(LocalDateTime now) {
        return popupHiddenForever || (popupHiddenUntil != null && popupHiddenUntil.isAfter(now));
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public boolean isRead() {
        return readAt != null;
    }

}
