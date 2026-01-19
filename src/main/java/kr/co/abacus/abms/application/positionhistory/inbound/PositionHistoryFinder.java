package kr.co.abacus.abms.application.positionhistory.inbound;

import java.util.List;

import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

/**
 * 직급 이력 조회
 */
public interface PositionHistoryFinder {

    PositionHistory findLast(Long employeeId);

    List<PositionHistory> findAll(Long employeeId);

}
