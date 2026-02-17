package kr.co.abacus.abms.application.summary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import kr.co.abacus.abms.application.employee.outbound.EmployeeCostPolicyRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryManager;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyRevenueSummaryModifyService implements MonthlyRevenueSummaryManager {

    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository projectRevenuePlanRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeCostPolicyRepository employeeCostPolicyRepository;
    private final PayrollRepository payrollRepository;

    @Override
    public MonthlyRevenueSummary calculateMonthlySummary(LocalDate targetMonth) {
        return null;
    }

    // 매출 계산: 해당 월에 발행된(isIssued) 매출 합계
    @Override
    public Money calculateRevenue(LocalDate targetMonth) {
        LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
        LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        List<ProjectRevenuePlan> revenuePlans = projectRevenuePlanRepository
            .findByRevenueDateBetweenAndIsIssuedTrue(startOfMonth, endOfMonth);

        return revenuePlans.stream()
            .map(ProjectRevenuePlan::getAmount)
            .reduce(Money.zero(), Money::add);
    }

    // 인력 비용 계산: 해당 월 투입 인력들의 총 비용 합계
    @Override
    public Money calculateTotalCost(LocalDate targetMonth) {
        LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
        LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        Money totalCost = Money.zero();

        List<ProjectAssignment> assignments = projectAssignmentRepository
            .findActiveAssignments(startOfMonth, endOfMonth);

        for (ProjectAssignment assignment : assignments) {
            Money onePersonCost = calculateOneEmployeeAssignmentCost(assignment, targetMonth);  // 인원 한 명당 비용 계산
            totalCost = totalCost.add(onePersonCost);
        }

        return totalCost;
    }

    // 인력 비용 계산: 인력 한 명에 대한 비용 계산
    public Money calculateOneEmployeeAssignmentCost(ProjectAssignment assignment, LocalDate targetMonth) {
        // M/M 계산 (엔티티에게 위임)
        BigDecimal mm = assignment.calculateManMonth(targetMonth);

        if (mm.compareTo(BigDecimal.ZERO) <= 0) {
            return Money.zero();
        }

        // 직원 정보 조회
        Employee employee = employeeRepository.findById(assignment.getEmployeeId())
            .orElseThrow(() -> new IllegalStateException("직원 정보 없음: " + assignment.getEmployeeId()));

        // 원가 정책 조회 (없으면 기본 정책)
        EmployeeCostPolicy costPolicy = employeeCostPolicyRepository.findByApplyYearAndType(targetMonth.getYear(), employee.getType())
            .orElseThrow(() -> new IllegalStateException("비용 정보 없음: " + employee.getType()));

        // 월 원가(단가) 계산
        Payroll payroll = payrollRepository.findCurrentSalaryByEmployeeId(employee.getId())
            .orElseThrow(() -> new IllegalStateException("급여 정보 없음: " + employee.getId()));

        Money monthlyUnitCost = costPolicy.calculateEmployeeCost(payroll.getAnnualSalary());

        // 최종 비용 = 단가 * M/M
        Money result = monthlyUnitCost.multiply(mm);

        return result;
    }

    @Override
    public Money calculateProfit(Money revenue, Money cost) {
        Money safeRevenue = (revenue != null) ? revenue : Money.zero();
        Money safeCost = (cost != null) ? cost : Money.zero();
        Money profit = safeRevenue.subtract(safeCost);

        return profit;
    }

    // 해당 월에 실적이 있는 프로젝트별로 집계 리스트를 반환
    @Override
    public List<MonthlyRevenueSummary> calculateMonthlySummaryByProject(LocalDate targetMonth) {
        List<MonthlyRevenueSummary> resultList = new ArrayList<>();

        LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
        LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        // 1. 해당 월에 진행 중인(Active) 프로젝트 목록 조회
        List<Project> activeProjects = projectRepository.findActiveProjects(startOfMonth, endOfMonth);

        // 2. 프로젝트 리스트 반복 (Loop)
        for (Project project : activeProjects) {
            // 2-1. 해당 프로젝트의 매출 계산
            Money revenue = calculateProjectRevenue(project, targetMonth);

            // 2-2. 해당 프로젝트의 비용 계산
            Money cost = calculateProjectTotalCost(project, targetMonth);

            // 2-3. 이익 계산
            Money profit = calculateProfit(revenue, cost);

            // 2-4. 결과 객체 생성 및 추가
            MonthlyRevenueSummary summary = MonthlyRevenueSummary.create(
                new MonthlyRevenueSummaryCreateRequest(
                    project.getId(),
                    targetMonth,
                    revenue,
                    cost,
                    profit
                )
            );

            resultList.add(summary);
        }

        return resultList;
    }

    public MonthlyRevenueSummary calculateSummaryForProject(Project project, LocalDate targetMonth) {
        // 1. 해당 프로젝트의 매출 계산
        Money revenue = calculateProjectRevenue(project, targetMonth);

        // 2. 해당 프로젝트의 비용 계산
        Money cost = calculateProjectTotalCost(project, targetMonth);

        // 3. 이익 계산
        Money profit = calculateProfit(revenue, cost);

        // 4. 결과 엔티티 생성
        return MonthlyRevenueSummary.create(
            new MonthlyRevenueSummaryCreateRequest(
                project.getId(),
                targetMonth,
                revenue,
                cost,
                profit
            )
        );
    }

    // 해당 월의 특정 프로젝트 매출 계산
    private Money calculateProjectRevenue(Project project, LocalDate targetMonth) {
        LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
        LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        List<ProjectRevenuePlan> revenuePlans = projectRevenuePlanRepository
            .findByProjectIdAndRevenueDateBetweenAndIsIssuedTrue(project.getId(), startOfMonth, endOfMonth);

        return revenuePlans.stream()
            .map(ProjectRevenuePlan::getAmount)
            .reduce(Money.zero(), Money::add);
    }

    // 해당 월의 특정 프로젝트 비용 계산
    private Money calculateProjectTotalCost(Project project, LocalDate targetMonth) {
        LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
        LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        Money totalCost = Money.zero();

        List<ProjectAssignment> assignments = projectAssignmentRepository
            .findActiveAssignmentsByProjectId(project.getId(), startOfMonth, endOfMonth);

        for (ProjectAssignment assignment : assignments) {
            Money onePersonCost = calculateOneEmployeeAssignmentCost(assignment, targetMonth);
            totalCost = totalCost.add(onePersonCost);
        }

        return totalCost;
    }

}
