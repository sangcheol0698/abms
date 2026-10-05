package kr.co.abacus.abms.notice;

import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            select n from Notice n
            where (n.startsAt is null or n.startsAt <= :now) and (n.endsAt is null or n.endsAt > :now)""")
    List<Notice> findAllPublished(@Param("now") LocalDateTime now);

    /**
     * 목록 검색: 상단 고정 → 게시일 최신 순.
     * status: ACTIVE(게시 중) · SCHEDULED(예약) · ENDED(종료) · null(전체, 관리자만 넘긴다)
     */
    @Query("""
            select n from Notice n
            where (:status is null
                    or (:status = 'ACTIVE' and (n.startsAt is null or n.startsAt <= :now) and (n.endsAt is null or n.endsAt > :now))
                    or (:status = 'SCHEDULED' and n.startsAt > :now)
                    or (:status = 'ENDED' and (n.startsAt is null or n.startsAt <= :now) and n.endsAt <= :now))
              and (:pattern is null or lower(n.title) like :pattern escape '\\' or lower(n.body) like :pattern escape '\\')
              and (:importance is null or n.importance = :importance)
              and (:unreadFor is null or not exists (
                    select r.id from NoticeReceipt r where r.noticeId = n.id and r.accountId = :unreadFor and r.readAt is not null))
            order by n.pinned desc, coalesce(n.startsAt, n.createdAt) desc, n.id desc""")
    Page<Notice> search(@Param("now") LocalDateTime now, @Nullable @Param("status") String status, @Nullable @Param("pattern") String pattern,
                        @Nullable @Param("importance") NoticeImportance importance, @Nullable @Param("unreadFor") Long unreadFor,
                        Pageable pageable);

}
