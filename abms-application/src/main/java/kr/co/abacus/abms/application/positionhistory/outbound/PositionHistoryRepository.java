package kr.co.abacus.abms.application.positionhistory.outbound;

import java.util.List;

import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

public interface PositionHistoryRepository {

    PositionHistory save(PositionHistory positionHistory);

    List<PositionHistory> findByEmployeeId(Long employeeId);

}
