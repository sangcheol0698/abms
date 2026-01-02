package kr.co.abacus.abms.application.positionhistory.inbound;

import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

/**
 * 직급 이력 조회
 */
public interface PositionHistoryFinder {

    PositionHistory findLast(Long employeeId);

}
