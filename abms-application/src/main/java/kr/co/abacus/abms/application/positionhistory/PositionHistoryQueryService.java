package kr.co.abacus.abms.application.positionhistory;

import java.util.List;

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
        return positionHistoryRepository.findByEmployeeId(employeeId).getLast();
    }

    @Override
    public List<PositionHistory> findAll(Long employeeId) {
        return positionHistoryRepository.findByEmployeeId(employeeId);
    }

}
