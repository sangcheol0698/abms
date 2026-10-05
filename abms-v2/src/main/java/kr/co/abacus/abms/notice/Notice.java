package kr.co.abacus.abms.notice;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 공지사항. 게시 기간 안에서만 사용자에게 보이고, popup 이면 안내 팝업으로도 띄운다.
 */
@Entity
@Table(name = "tb_notice")
@SQLRestriction("deleted = false")
public class Notice extends BaseEntity implements Auditable {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeImportance importance = NoticeImportance.NORMAL;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private boolean popup;

    private @Nullable LocalDateTime startsAt;

    private @Nullable LocalDateTime endsAt;

    protected Notice() {
    }

    public static Notice create(NoticeInfo info) {
        Notice notice = new Notice();
        notice.apply(info);
        return notice;
    }

    public void update(NoticeInfo info) {
        apply(info);
    }

    private void apply(NoticeInfo info) {
        String title = info.title() == null ? "" : info.title().trim();
        if (title.isEmpty()) {
            throw new BusinessException("공지 제목을 입력하세요.");
        }
        if (title.length() > 100) {
            throw new BusinessException("공지 제목은 100자 이하로 입력하세요.");
        }
        String body = info.body() == null ? "" : info.body().strip();
        if (body.isEmpty()) {
            throw new BusinessException("공지 내용을 입력하세요.");
        }
        if (body.length() > 20_000) {
            throw new BusinessException("공지 내용은 20,000자 이하로 입력하세요.");
        }
        if (info.startsAt() != null && info.endsAt() != null && !info.endsAt().isAfter(info.startsAt())) {
            throw new BusinessException("게시 종료는 게시 시작보다 늦어야 합니다.");
        }
        this.title = title;
        this.body = body;
        this.importance = info.importance() == null ? NoticeImportance.NORMAL : info.importance();
        this.pinned = info.pinned();
        this.popup = info.popup();
        this.startsAt = info.startsAt();
        this.endsAt = info.endsAt();
    }

    /** 게시 기간 안인지 */
    public boolean isPublishedAt(LocalDateTime now) {
        return (startsAt == null || !startsAt.isAfter(now)) && (endsAt == null || endsAt.isAfter(now));
    }

    /** 관리 화면용 상태: 예약 / 게시 중 / 종료 */
    public String statusAt(LocalDateTime now) {
        if (startsAt != null && startsAt.isAfter(now)) {
            return "예약";
        }
        return endsAt != null && !endsAt.isAfter(now) ? "종료" : "게시 중";
    }

    /** 목록·팝업에 표시하는 게시일 (게시 시작, 없으면 등록일) */
    public @Nullable LocalDateTime publishedAt() {
        return startsAt != null ? startsAt : getCreatedAt();
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public NoticeImportance getImportance() {
        return importance;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isPopup() {
        return popup;
    }

    public @Nullable LocalDateTime getStartsAt() {
        return startsAt;
    }

    public @Nullable LocalDateTime getEndsAt() {
        return endsAt;
    }

    @Override
    public String auditLabel() {
        return "공지사항";
    }

    @Override
    public String auditName() {
        return title;
    }

    public record NoticeInfo(@Nullable String title, @Nullable String body, @Nullable NoticeImportance importance, boolean pinned,
                             boolean popup, @Nullable LocalDateTime startsAt, @Nullable LocalDateTime endsAt) {
    }

}
