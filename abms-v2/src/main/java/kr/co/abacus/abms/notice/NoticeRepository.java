package kr.co.abacus.abms.notice;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 게시 기간 안의 공지: 상단 고정 → 게시일 최신 순 */
    @Query("""
            select n from Notice n
            where (n.startsAt is null or n.startsAt <= :now) and (n.endsAt is null or n.endsAt > :now)
            order by n.pinned desc, coalesce(n.startsAt, n.createdAt) desc, n.id desc""")
    Page<Notice> findPublished(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
            select n from Notice n
            where (n.startsAt is null or n.startsAt <= :now) and (n.endsAt is null or n.endsAt > :now)""")
    List<Notice> findAllPublished(@Param("now") LocalDateTime now);

    /** 관리자: 예약·종료 포함 전체 */
    @Query("select n from Notice n order by n.pinned desc, coalesce(n.startsAt, n.createdAt) desc, n.id desc")
    Page<Notice> findAllForManage(Pageable pageable);

}
