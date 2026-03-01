package kr.co.abacus.abms.application.positionhistory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.application.positionhistory.inbound.PositionHistoryManager;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class PositionHistoryService implements PositionHistoryManager {

    private final PositionHistoryRepository positionHistoryRepository;

    @Override
    public PositionHistory create(PositionHistoryCreateRequest createRequest) {
        PositionHistory positionHistory = PositionHistory.create(createRequest);

        positionHistoryRepository.save(positionHistory);

        return positionHistory;
    }

}
