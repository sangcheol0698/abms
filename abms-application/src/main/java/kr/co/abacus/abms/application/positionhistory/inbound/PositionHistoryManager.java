package kr.co.abacus.abms.application.positionhistory.inbound;

import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

/**
 * 직급 이력 생성
 */
public interface PositionHistoryManager {

    PositionHistory create(PositionHistoryCreateRequest createRequest);

}
