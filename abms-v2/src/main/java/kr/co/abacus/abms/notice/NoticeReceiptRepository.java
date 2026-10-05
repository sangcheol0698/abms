package kr.co.abacus.abms.notice;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeReceiptRepository extends JpaRepository<NoticeReceipt, Long> {

    Optional<NoticeReceipt> findByNoticeIdAndAccountId(Long noticeId, Long accountId);

    List<NoticeReceipt> findAllByAccountIdAndNoticeIdIn(Long accountId, Collection<Long> noticeIds);

}
