package kr.co.abacus.abms.application.project.outbound;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class ProjectRevenuePlanRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ProjectRevenuePlanRepository revenuePlanRepository;

    @Test
    @DisplayName("특정 프로젝트의 해당 기간 내 발행된 매출 합계만 계산한다")
    void sumIssuedRevenueByProject_Success() {
        // given
        Long targetProjectId = 100L;
        Long otherProjectId = 200L;

        LocalDate startOfMonth = LocalDate.of(2024, 3, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 3, 31);

        // 1. 조건에 완벽히 부합하는 데이터 (합산 대상)
        // 3월 5일, 1000원, 발행됨
        saveRevenuePlan(targetProjectId, 1, LocalDate.of(2024, 3, 5), RevenueType.DOWN_PAYMENT, 1000L);
        // 3월 31일, 2000원, 발행됨
        saveRevenuePlan(targetProjectId, 2, LocalDate.of(2024, 3, 31), RevenueType.INTERMEDIATE_PAYMENT, 2000L);

        // 2. [Noise] 조건 불일치 데이터 (합산 제외 대상)
    }

    private void saveRevenuePlan(Long projectId, Integer sequence, LocalDate revenueDate, RevenueType revenueType, Long amount) {
        ProjectRevenuePlan revenuePlan = ProjectRevenuePlan.create(
            new ProjectRevenuePlanCreateRequest(
                projectId,
                sequence,
                revenueDate,
                revenueType,
                amount,
                null
            )
        );
        revenuePlan.issue();

        revenuePlanRepository.save(revenuePlan);
        flushAndClear();
    }
}