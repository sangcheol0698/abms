package kr.co.abacus.abms.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 월 손익 집계 규칙 검증. (2026년 정직원 원가 정책: 제경비 10%, 판관비 5% → 월급 × 1.15)
 */
@IntegrationTest
class ProfitCalculationServiceTest {

    private static final YearMonth FEB = YearMonth.of(2026, 2);

    @Autowired
    private ProfitCalculationService calculationService;

    @Autowired
    private MonthlyRevenueSummaryRepository summaryRepository;

    @Autowired
    private CompanyMonthlyCostSummaryRepository companySummaryRepository;

    @Autowired
    private EmployeeMonthlyCostRepository monthlyCostRepository;

    @Autowired
    private MonthClosingService closingService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private Fixtures fixtures;

    private Department lead;
    private Employee leadMember;
    private Employee supporter;
    private Employee idle;
    private Project project;

    @BeforeEach
    void setUp() {
        Department root = fixtures.department("회사", null);
        lead = fixtures.department("주관팀", root);
        Department support = fixtures.department("지원팀", root);

        leadMember = fixtures.employee(lead, "주관팀원");
        supporter = fixtures.employee(support, "지원팀원");
        idle = fixtures.employee(support, "대기인력");
        fixtures.payroll(leadMember, 120_000_000, LocalDate.of(2025, 1, 1)); // 월 1,000만 → 원가 1,150만
        fixtures.payroll(supporter, 60_000_000, LocalDate.of(2025, 1, 1));   // 월 500만 → 원가 575만
        fixtures.payroll(idle, 36_000_000, LocalDate.of(2025, 1, 1));        // 월 300만 → 원가 345만

        project = fixtures.project(lead, 1_000_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        fixtures.revenue(project, 1, LocalDate.of(2026, 2, 10), 100_000_000, true);
        fixtures.revenue(project, 2, LocalDate.of(2026, 2, 20), 50_000_000, false);   // 미발행 → 제외
        fixtures.revenue(project, 3, LocalDate.of(2026, 3, 10), 200_000_000, true);   // 다른 월 → 제외
        fixtures.assign(project, leadMember, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));   // 1.0 M/M
        fixtures.assign(project, supporter, LocalDate.of(2026, 2, 15), LocalDate.of(2026, 2, 28));   // 0.5 M/M
    }

    @Test
    void 발행된_매출과_투입_MM_기준_비용을_주관_부서에_귀속한다() {
        CalculationResult result = calculationService.calculate(FEB);

        assertThat(result.skipped()).isFalse();
        assertThat(result.warnings()).isEmpty();
        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        assertThat(summary.getTargetMonth()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(summary.getRevenueAmount()).isEqualTo(Money.wons(100_000_000));
        assertThat(summary.getCostAmount()).isEqualTo(Money.wons(11_500_000 + 2_875_000));
        assertThat(summary.getProfitAmount()).isEqualTo(Money.wons(100_000_000 - 14_375_000));
        assertThat(summary.getLeadDepartmentId()).isEqualTo(lead.id()); // 지원 인력 비용도 주관 부서로
    }

    @Test
    void 직접비는_귀속일이_속한_월의_프로젝트_비용에_더한다() {
        fixtures.expense(project, LocalDate.of(2026, 2, 5), 3_000_000);
        fixtures.expense(project, LocalDate.of(2026, 2, 28), 2_000_000);
        fixtures.expense(project, LocalDate.of(2026, 3, 1), 9_000_000);   // 다른 월 → 제외

        calculationService.calculate(FEB);

        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        assertThat(summary.getLaborCostAmount()).isEqualTo(Money.wons(14_375_000));
        assertThat(summary.getDirectCostAmount()).isEqualTo(Money.wons(5_000_000));
        assertThat(summary.getCostAmount()).isEqualTo(Money.wons(19_375_000));
        assertThat(summary.getProfitAmount()).isEqualTo(Money.wons(100_000_000 - 19_375_000));
    }

    @Test
    void 직접비만_있는_프로젝트도_집계한다() {
        Project expenseOnly = fixtures.project(lead, 0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        fixtures.expense(expenseOnly, LocalDate.of(2026, 2, 10), 1_000_000);   // 종료 후 발생한 하자 보수 비용

        calculationService.calculate(FEB);

        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(expenseOnly.id()));
        assertThat(summary.getDirectCostAmount()).isEqualTo(Money.wons(1_000_000));
        assertThat(summary.getProfitAmount()).isEqualTo(Money.wons(-1_000_000));
    }

    @Test
    void 투입률만큼_인건비와_전사_배분액을_나눈다() {
        Project other = fixtures.project(lead, 100_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        fixtures.assign(project, idle, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), 30);   // 345만 × 0.3
        fixtures.assign(other, idle, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), 50);     // 345만 × 0.5

        calculationService.calculate(FEB);

        MonthlyRevenueSummary main = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        MonthlyRevenueSummary sub = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(other.id()));
        assertThat(main.getLaborCostAmount()).isEqualTo(Money.wons(14_375_000 + 1_035_000));
        assertThat(sub.getLaborCostAmount()).isEqualTo(Money.wons(1_725_000));
        CompanyMonthlyCostSummary company = companySummaryRepository.findByTargetMonth(FEB.atDay(1)).orElseThrow();
        assertThat(company.getUnallocatedFullTimeCost()).isEqualTo(Money.wons(2_875_000 + 690_000));   // 지원팀원 절반 + 대기인력 남은 20%
    }

    @Test
    void 관리_매출은_계약금액을_프로젝트_기간에_일할한다() {
        calculationService.calculate(FEB);

        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        // 10억 × 59/365 − 10억 × 31/365 (2026-01-01 ~ 12-31, 누적 일할의 차이)
        assertThat(summary.getManagedRevenueAmount()).isEqualTo(Money.wons(161_643_836 - 84_931_507));
        assertThat(summary.getRevenueAmount()).isEqualTo(Money.wons(100_000_000));   // 청구 기준은 그대로
        assertThat(summary.getManagedProfitAmount()).isEqualTo(Money.wons(76_712_329 - 14_375_000));
    }

    @Test
    void 취소된_프로젝트의_관리_매출은_청구_기준과_같다() {
        projectService.cancel(Fixtures.admin(leadMember), project.id());

        calculationService.calculate(FEB);

        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        assertThat(summary.getManagedRevenueAmount()).isEqualTo(Money.wons(100_000_000));
    }

    @Test
    void 보류된_프로젝트의_관리_매출도_청구_기준과_같다() {
        project.update(new Project.ProjectInfo(project.getPartyId(), project.getLeadDepartmentId(), project.getName(), project.getDescription(),
                ProjectStatus.ON_HOLD, project.getContractAmount(), project.getPeriod()));

        calculationService.calculate(FEB);

        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        assertThat(summary.getManagedRevenueAmount()).isEqualTo(Money.wons(100_000_000));
    }

    @Test
    void 직원_월_원가를_스냅샷으로_저장한다() {
        calculationService.calculate(FEB);

        EmployeeMonthlyCost cost = monthlyCostRepository.findByEmployeeIdAndTargetMonth(leadMember.id(), FEB.atDay(1)).orElseThrow();
        assertThat(cost.getMonthlySalary()).isEqualTo(Money.wons(10_000_000));
        assertThat(cost.getOverheadCost()).isEqualTo(Money.wons(1_000_000));
        assertThat(cost.getSgaCost()).isEqualTo(Money.wons(500_000));
        assertThat(cost.getTotalCost()).isEqualTo(Money.wons(11_500_000));
    }

    @Test
    void 프로젝트에_배분되지_않은_정직원_비용을_집계한다() {
        calculationService.calculate(FEB);

        CompanyMonthlyCostSummary company = companySummaryRepository.findByTargetMonth(FEB.atDay(1)).orElseThrow();
        assertThat(company.getTotalFullTimeCost()).isEqualTo(Money.wons(11_500_000 + 5_750_000 + 3_450_000));
        assertThat(company.getAllocatedFullTimeCost()).isEqualTo(Money.wons(14_375_000));
        assertThat(company.getUnallocatedFullTimeCost()).isEqualTo(Money.wons(6_325_000));
    }

    @Test
    void 월중_입사자와_퇴사자의_총원가는_재직일수만큼_일할한다() {
        Employee newcomer = fixtures.employee(lead, "신규입사", EmployeeType.FULL_TIME, LocalDate.of(2026, 2, 15));
        fixtures.payroll(newcomer, 120_000_000, LocalDate.of(2026, 2, 15));   // 원가 1,150만 × 0.5 (14/28)
        Employee leaver = fixtures.employee(lead, "퇴사예정");
        fixtures.payroll(leaver, 120_000_000, LocalDate.of(2025, 1, 1));
        leaver.resign(LocalDate.of(2026, 2, 7));                               // 원가 1,150만 × 0.3 (7/28 = 0.25 → 0.3)

        calculationService.calculate(FEB);

        CompanyMonthlyCostSummary company = companySummaryRepository.findByTargetMonth(FEB.atDay(1)).orElseThrow();
        assertThat(company.getTotalFullTimeCost())
                .isEqualTo(Money.wons(11_500_000 + 5_750_000 + 3_450_000 + 5_750_000 + 3_450_000));
        assertThat(company.getUnallocatedFullTimeCost()).isEqualTo(Money.wons(6_325_000 + 5_750_000 + 3_450_000));
    }

    @Test
    void 입사일부터_전부_투입된_월중_입사자는_미배분_비용이_없다() {
        Employee newcomer = fixtures.employee(lead, "신규입사", EmployeeType.FULL_TIME, LocalDate.of(2026, 2, 15));
        fixtures.payroll(newcomer, 120_000_000, LocalDate.of(2026, 2, 15));
        fixtures.assign(project, newcomer, LocalDate.of(2026, 2, 15), LocalDate.of(2026, 2, 28));

        calculationService.calculate(FEB);

        CompanyMonthlyCostSummary company = companySummaryRepository.findByTargetMonth(FEB.atDay(1)).orElseThrow();
        assertThat(company.getUnallocatedFullTimeCost()).isEqualTo(Money.wons(6_325_000));
    }

    @Test
    void 같은_월을_다시_집계해도_결과가_같다() {
        calculationService.calculate(FEB);
        calculationService.calculate(FEB);

        assertThat(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id())).hasSize(1);
        assertThat(monthlyCostRepository.findAllByTargetMonth(FEB.atDay(1))).hasSize(3);
    }

    @Test
    void 마감된_월은_집계하지_않는다() {
        LoginUser admin = Fixtures.admin(leadMember);
        closingService.close(admin, FEB);
        fixtures.revenue(project, 4, LocalDate.of(2026, 2, 25), 30_000_000, true);

        CalculationResult result = calculationService.calculate(FEB);

        assertThat(result.skipped()).isTrue();
        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        assertThat(summary.getRevenueAmount()).isEqualTo(Money.wons(100_000_000)); // 마감 시점 값 유지
    }

    @Test
    void 삭제된_프로젝트의_집계는_재집계_시_제거된다() {
        Project empty = fixtures.project(lead, 100_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        calculationService.calculate(FEB);
        projectService.delete(Fixtures.admin(leadMember), empty.id());

        CalculationResult result = calculationService.calculate(FEB);

        assertThat(result.removedCount()).isEqualTo(1);
        assertThat(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(empty.id())).isEmpty();
        assertThat(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id())).hasSize(1);
    }

    @Test
    void 실적이_있는_프로젝트는_삭제할_수_없어_집계가_유지된다() {
        calculationService.calculate(FEB);

        assertThatThrownBy(() -> projectService.delete(Fixtures.admin(leadMember), project.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("취소 처리");

        calculationService.calculate(FEB);
        MonthlyRevenueSummary summary = only(summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(project.id()));
        assertThat(summary.getRevenueAmount()).isEqualTo(Money.wons(100_000_000));
    }

    @Test
    void 급여_정보가_없는_직원은_경고로_알린다() {
        fixtures.employee(lead, "급여없음");

        CalculationResult result = calculationService.calculate(FEB);

        assertThat(result.warnings()).anyMatch(w -> w.contains("급여 정보 없음") && w.contains("급여없음"));
    }

    private static <T> T only(List<T> items) {
        assertThat(items).hasSize(1);
        return items.getFirst();
    }

}
