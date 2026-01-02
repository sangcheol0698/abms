package kr.co.abacus.abms.application.positionhistory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.positionhistory.inbound.PositionHistoryFinder;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionHistoryQueryService implements PositionHistoryFinder {

    private final PositionHistoryRepository positionHistoryRepository;

    @Override
    public PositionHistory findLast(Long employeeId) {
        System.out.println("결과:" + positionHistoryRepository.findByEmployeeId(employeeId));
        return positionHistoryRepository.findByEmployeeId(employeeId).getLast();
    }

}
