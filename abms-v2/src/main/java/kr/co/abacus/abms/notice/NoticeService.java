package kr.co.abacus.abms.notice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.notice.Notice.NoticeInfo;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 공지사항. 조회는 로그인 사용자 누구나(게시 기간 안), 작성·수정·삭제와 예약·종료 공지 조회는 계정 관리 권한.
 */
@Service
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeReceiptRepository receiptRepository;

    public NoticeService(NoticeRepository noticeRepository, NoticeReceiptRepository receiptRepository) {
        this.noticeRepository = noticeRepository;
        this.receiptRepository = receiptRepository;
    }

    public static boolean canManage(LoginUser user) {
        return user.has(PermissionCode.ACCOUNT_MANAGE);
    }

    @Transactional(readOnly = true)
    public Page<Notice> list(LoginUser user, Pageable pageable) {
        return canManage(user) ? noticeRepository.findAllForManage(pageable) : noticeRepository.findPublished(LocalDateTime.now(), pageable);
    }

    /** 공지 상세. 게시 기간 밖의 공지는 관리자만 볼 수 있다. 열면 읽음으로 표시한다. */
    public Notice open(LoginUser user, Long id) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> NotFoundException.of("공지사항", id));
        LocalDateTime now = LocalDateTime.now();
        if (!notice.isPublishedAt(now) && !canManage(user)) {
            throw NotFoundException.of("공지사항", id);
        }
        receipt(id, user.accountId()).markRead(now);
        return notice;
    }

    @Transactional(readOnly = true)
    public Notice get(Long id) {
        return noticeRepository.findById(id).orElseThrow(() -> NotFoundException.of("공지사항", id));
    }

    /** 읽은 공지 id (목록의 안 읽음 표시용) */
    @Transactional(readOnly = true)
    public Set<Long> readIds(Long accountId, Collection<Long> noticeIds) {
        if (noticeIds.isEmpty()) {
            return Set.of();
        }
        return receiptRepository.findAllByAccountIdAndNoticeIdIn(accountId, noticeIds).stream()
                .filter(NoticeReceipt::isRead).map(NoticeReceipt::getNoticeId).collect(Collectors.toSet());
    }

    /** 게시 중인데 아직 읽지 않은 공지 수 */
    @Transactional(readOnly = true)
    public long unreadCount(Long accountId) {
        List<Long> ids = noticeRepository.findAllPublished(LocalDateTime.now()).stream().map(Notice::id).toList();
        return ids.size() - readIds(accountId, ids).size();
    }

    /**
     * 지금 띄울 안내 팝업: 팝업으로 등록되고 게시 중이며, 사용자가 숨기지 않은 공지 중 중요도 → 최신 순 첫 번째.
     *
     * @param closedInSession '닫기'로 이번 로그인 동안 숨긴 공지
     */
    @Transactional(readOnly = true)
    public Optional<Notice> nextPopup(Long accountId, Set<Long> closedInSession) {
        LocalDateTime now = LocalDateTime.now();
        List<Notice> candidates = noticeRepository.findAllPublished(now).stream()
                .filter(Notice::isPopup)
                .filter(n -> !closedInSession.contains(n.id()))
                .toList();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        Map<Long, NoticeReceipt> receipts = receiptRepository.findAllByAccountIdAndNoticeIdIn(accountId,
                candidates.stream().map(Notice::id).toList()).stream().collect(Collectors.toMap(NoticeReceipt::getNoticeId, Function.identity()));
        return candidates.stream()
                .filter(n -> receipts.get(n.id()) == null || !receipts.get(n.id()).popupHiddenAt(now))
                .min(Comparator.comparing(Notice::getImportance)
                        .thenComparing(Notice::publishedAt, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    /** 화면 공통 정보: 안 읽은 공지 수와 띄울 팝업이 있는지 */
    @Transactional(readOnly = true)
    public Summary summary(Long accountId, Set<Long> closedInSession) {
        return new Summary(unreadCount(accountId), nextPopup(accountId, closedInSession).isPresent());
    }

    public record Summary(long unread, boolean popup) {

        public static final Summary NONE = new Summary(0, false);

    }

    /** 오늘 하루 보지 않기: 오늘 자정까지 팝업을 숨긴다. */
    public void hidePopupToday(Long accountId, Long noticeId) {
        get(noticeId);
        receipt(noticeId, accountId).hidePopupUntil(LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT));
    }

    /** 다시 보지 않기 */
    public void hidePopupForever(Long accountId, Long noticeId) {
        get(noticeId);
        receipt(noticeId, accountId).hidePopupForever();
    }

    public Notice create(LoginUser user, NoticeInfo info) {
        requireManage(user);
        return noticeRepository.save(Notice.create(info));
    }

    public void update(LoginUser user, Long id, NoticeInfo info) {
        requireManage(user);
        get(id).update(info);
    }

    public void delete(LoginUser user, Long id) {
        requireManage(user);
        get(id).softDelete(user.accountId());
    }

    private NoticeReceipt receipt(Long noticeId, Long accountId) {
        return receiptRepository.findByNoticeIdAndAccountId(noticeId, accountId)
                .orElseGet(() -> receiptRepository.save(NoticeReceipt.of(noticeId, accountId)));
    }

    private static void requireManage(LoginUser user) {
        if (!canManage(user)) {
            throw new AccessDeniedException("'계정 관리' 권한이 있어야 공지를 관리할 수 있습니다.");
        }
    }

}
