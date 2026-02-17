package kr.co.abacus.abms.application.summary.inbound;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.application.employee.outbound.EmployeeCostPolicyRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("월별 매출 집계 (MonthlyRevenueSummaryManager)")
class MonthlyRevenueSummaryManagerTest extends IntegrationTestBase {

    @Autowired
    private MonthlyRevenueSummaryManager monthlyRevenueSummaryManager;

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeCostPolicyRepository employeeCostPolicyRepository;


    @Test
    @DisplayName("매출 계산: 해당 월에 발행된 매출 계획들의 금액을 모두 합산한다")
    void calculateRevenue_Success() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 20)));

        // 1000원짜리 계획과 2000원짜리 계획이 있다고 가정
        ProjectRevenuePlan plan1 = createProjectRevenuePlan(
            project.getId(),
            1,
            LocalDate.of(2026, 1, 1),
            RevenueType.DOWN_PAYMENT,
            1000L
        );
        ProjectRevenuePlan plan2 = createProjectRevenuePlan(
            project.getId(),
            2,
            LocalDate.of(2026, 2, 10),
            RevenueType.BALANCE_PAYMENT,
            2000L
        );

        plan1 = projectRevenuePlanRepository.save(plan1);
        plan2 = projectRevenuePlanRepository.save(plan2);

        plan1.issue();
        plan2.issue();

        flushAndClear();

        // when
        LocalDate targetMonth = LocalDate.of(2026, 2, 15); // 2월 기준
        Money result = monthlyRevenueSummaryManager.calculateRevenue(targetMonth);
        System.out.println("result = " + result);

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(2000L));
    }

    @Test
    @DisplayName("매출 계산: 해당 월에 발행된 매출 계획이 없거나(다른 달/미발행), 데이터가 아예 없으면 0원을 반환한다")
    void calculateRevenue_Empty() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        // 기간 설정 (테스트 로직상 크게 중요하진 않으나 정합성 유지)
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        // Case 1: 1월(다른 달)에 발행된 매출 계획 -> 2월 집계 대상 아님
        ProjectRevenuePlan otherMonthPlan = createProjectRevenuePlan(
            project.getId(),
            1,
            LocalDate.of(2026, 1, 15),
            RevenueType.DOWN_PAYMENT,
            1000L
        );
        projectRevenuePlanRepository.save(otherMonthPlan);
        otherMonthPlan.issue(); // 발행 처리

        // Case 2: 2월(이번 달)이지만 아직 발행 안 된(isIssued=false) 매출 계획 -> 집계 대상 아님
        ProjectRevenuePlan unissuedPlan = createProjectRevenuePlan(
            project.getId(),
            2,
            LocalDate.of(2026, 2, 15),
            RevenueType.BALANCE_PAYMENT,
            5000L
        );
        projectRevenuePlanRepository.save(unissuedPlan);

        flushAndClear();

        // when
        LocalDate targetMonth = LocalDate.of(2026, 2, 20); // 2월 기준 조회
        Money result = monthlyRevenueSummaryManager.calculateRevenue(targetMonth);

        // then
        // 1월 발행건(1000원) 제외, 2월 미발행건(5000원) 제외 -> 결과는 0원이어야 함
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("비용 계산: 해당 월에 투입된 모든 인력의 비용을 합산한다")
    void calculateTotalCost_Success() {
        // given
        // 1. 프로젝트 생성
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 10L);
         ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)));
        // project = projectRepository.save(project);

        // 2. 직원 및 급여/정책 데이터 세팅 (헬퍼 메서드 활용)
        Employee empA = saveEmployeeWithCost("이직원", "this@naver.com", EmployeeType.FULL_TIME, 5_000_000L, 0.1, 0.05);
        Employee empB = saveEmployeeWithCost("저직원", "that@naver.com", EmployeeType.FREELANCER, 3_000_000L, 0.04, 0.02);

        // 3. 프로젝트 할당 생성
        // 직원A: 2월 꽉 채움 (1.0 MM)
        ProjectAssignment assignmentA = ProjectAssignment.assign(
            project,
            new ProjectAssignmentCreateRequest(
                project.getId(), empA.getId(), AssignmentRole.DEV,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)
            )
        );

        // 직원B: 2월 절반 (0.5 MM)
        ProjectAssignment assignmentB = ProjectAssignment.assign(
            project,
            new ProjectAssignmentCreateRequest(
                project.getId(), empB.getId(), AssignmentRole.DEV,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 14)
            )
        );

        projectAssignmentRepository.saveAll(List.of(assignmentA, assignmentB));
        flushAndClear();

        // when
        LocalDate targetMonth = LocalDate.of(2026, 2, 15); // 2월 기준
        Money result = monthlyRevenueSummaryManager.calculateTotalCost(targetMonth);
        System.out.println("Total Cost Result = " + result);

        // then
        // 500만 * (1 + 0.1 + 0.05) * 1.0 MM + 300만 * (1 + 0.04 + 0.02) * 0.5 MM = 575만 + 159만 = 734만
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(7_340_000L));
    }

    private ProjectRevenuePlan createProjectRevenuePlan(Long projectId, Integer sequence, LocalDate revenueDate, RevenueType type, Long amount) {
        return ProjectRevenuePlan.create(
            new ProjectRevenuePlanCreateRequest(projectId, sequence, revenueDate, type, amount, null)
        );
    }

    // 직원 + 급여 + 정책을 한방에 저장하는 헬퍼
    private Employee saveEmployeeWithCost(String name, String email, EmployeeType type, Long monthlyCost, Double overheadRate, Double sgaRate) {
        // 1. 직원 저장
        Employee employee = createEmployee(name, email, type);
        employee = employeeRepository.save(employee);

        // 2. 급여 저장
        Payroll payroll = Payroll.create(
            employee.getId(), Money.wons(monthlyCost * 12), LocalDate.of(2026, 1, 1)
        );
        payrollRepository.save(payroll);

        // 3. 비용 정책 저장
        if (employeeCostPolicyRepository.findByApplyYearAndType(2026, type).isEmpty()) {
            EmployeeCostPolicy policy = EmployeeCostPolicy.create(2026, type, overheadRate, sgaRate);
            employeeCostPolicyRepository.save(policy);
        }

        return employee;
    }

    @Test
    @DisplayName("이익 계산: 매출에서 비용을 뺀 금액이 이익이다 (흑자/적자/본전)")
    void calculateProfit_Basic() {
        // 1. 흑자 (매출 1000 > 비용 700)
        Money revenue1 = Money.wons(1000L);
        Money cost1 = Money.wons(700L);

        Money profit1 = monthlyRevenueSummaryManager.calculateProfit(revenue1, cost1);
        assertThat(profit1.amount()).isEqualByComparingTo(BigDecimal.valueOf(300L));

        // 2. 적자 (매출 500 < 비용 800) -> -300원
        Money revenue2 = Money.wons(500L);
        Money cost2 = Money.wons(800L);

        Money profit2 = monthlyRevenueSummaryManager.calculateProfit(revenue2, cost2);
        assertThat(profit2.amount()).isEqualByComparingTo(BigDecimal.valueOf(-300L));

        // 3. 본전 (매출 100 == 비용 100) -> 0원
        Money revenue3 = Money.wons(100L);
        Money cost3 = Money.wons(100L);

        Money profit3 = monthlyRevenueSummaryManager.calculateProfit(revenue3, cost3);
        assertThat(profit3.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("프로젝트별 월별 집계: 진행 중인 여러 프로젝트의 매출/비용/이익을 각각 계산하여 리스트로 반환한다")
    void calculateMonthlySummaryByProject_Success() {
        // given
        // 1. [프로젝트 A] (매출 O, 비용 O -> 흑자)
        Project projectA = projectRepository.save(
            Project.create(
                new ProjectCreateRequest(
                    1L,
                    10L,
                    "PRJ-0001",
                    "차세대 빌링 구축",
                    "차세대 빌링 구축 시스템입니다.",
                    ProjectStatus.IN_PROGRESS,
                    10_000_000L,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 6, 30)
                )
            )
        );

    }

}