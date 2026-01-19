package kr.co.abacus.abms.adapter.api.positionhistory;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.application.positionhistory.inbound.PositionHistoryFinder;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PositionHistoryApi {

    private final PositionHistoryFinder positionHistoryFinder;

    private static final String SYSTEM_DELETER = "SYSTEM"; // TODO: 추후 인증/인가 기능 도입 시 수정 필요

    @GetMapping("/api/positionHistory/{employeeId}")
    public List<PositionHistory> findAll(@PathVariable Long employeeId) {
        List<PositionHistory> foundPositionHistoryList = positionHistoryFinder.findAll(employeeId);

        return foundPositionHistoryList;
    }

}
