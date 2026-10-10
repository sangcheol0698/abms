package kr.co.abacus.abms.security;

import java.time.LocalDate;
import java.util.List;

/**
 * 프로젝트 참여 조회. "현재 참여" 범위를 해석할 때 쓴다. 투입 인력 리포지토리가 구현한다.
 */
public interface ParticipationLookup {

    /** 해당 날짜에 투입 중인 프로젝트 id */
    List<Long> findActiveProjectIds(Long employeeId, LocalDate date);

}
