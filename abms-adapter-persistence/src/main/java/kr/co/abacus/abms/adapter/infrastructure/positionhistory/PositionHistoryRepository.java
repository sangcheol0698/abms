package kr.co.abacus.abms.adapter.infrastructure.positionhistory;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

public interface PositionHistoryRepository
        extends Repository<PositionHistory, Long>,
        kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository {
}
