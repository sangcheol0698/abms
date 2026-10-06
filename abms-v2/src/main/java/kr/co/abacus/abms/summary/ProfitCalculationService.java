package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.employee.Payroll;
import kr.co.abacus.abms.employee.PayrollRepository;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentRepository;
import kr.co.abacus.abms.project.ProjectExpense;
import kr.co.abacus.abms.project.ProjectExpenseRepository;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.summary.EmployeeCostPolicy.CostBreakdown;
import kr.co.abacus.abms.summary.MonthlyRevenueSummary.Snapshot;

/**
 * 월 손익 집계.
 * <ol>
 *     <li>직원 월 원가 = 월 기본급(연봉/12) × (1 + 제경비율 + 판관비율)</li>
 *     <li>프로젝트 매출(청구 기준) = 해당 월 청구일 + 발행 완료된 매출 계획 합계</li>
 *     <li>프로젝트 관리 매출(진행 기준) = 계약금액을 프로젝트 기간에 일할 배분한 해당 월 몫 (취소된 프로젝트는 청구 기준과 같다)</li>
 *     <li>프로젝트 비용 = 인건비 Σ(투입 직원 월 원가 × 투입 M/M × 투입률) + 해당 월 귀속 직접비</li>
 *     <li>손익은 프로젝트 <b>주관 부서</b>에 귀속</li>
 *     <li>전사 정직원 비용 중 프로젝트에 배분되지 않은 금액을 별도 집계</li>
 * </ol>
 * 같은 월을 여러 번 집계해도 결과가 같다(멱등). 마감된 월은 집계하지 않는다.
 */
@Service
public class ProfitCalculationService {

    private static final Logger log = LoggerFactory.getLogger(ProfitCalculationService.class);

    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeCostPolicyRepository policyRepository;
    private final EmployeeMonthlyCostRepository monthlyCostRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final ProjectExpenseRepository expenseRepository;
    private final DepartmentRepository departmentRepository;
    private final MonthlyRevenueSummaryRepository summaryRepository;
    private final CompanyMonthlyCostSummaryRepository companySummaryRepository;
    private final RevenueMonthClosingRepository closingRepository;

    public ProfitCalculationService(EmployeeRepository employeeRepository, PayrollRepository payrollRepository,
                                    EmployeeCostPolicyRepository policyRepository,
                                    EmployeeMonthlyCostRepository monthlyCostRepository,
                                    ProjectRepository projectRepository,
                                    ProjectRevenuePlanRepository revenuePlanRepository,
                                    ProjectAssignmentRepository assignmentRepository,
                                    ProjectExpenseRepository expenseRepository,
                                    DepartmentRepository departmentRepository,
                                    MonthlyRevenueSummaryRepository summaryRepository,
                                    CompanyMonthlyCostSummaryRepository companySummaryRepository,
                                    RevenueMonthClosingRepository closingRepository) {
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
        this.policyRepository = policyRepository;
        this.monthlyCostRepository = monthlyCostRepository;
        this.projectRepository = projectRepository;
        this.revenuePlanRepository = revenuePlanRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenseRepository = expenseRepository;
        this.departmentRepository = departmentRepository;
        this.summaryRepository = summaryRepository;
        this.companySummaryRepository = companySummaryRepository;
        this.closingRepository = closingRepository;
    }

    @Transactional
    public CalculationResult calculate(YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        if (closingRepository.existsByTargetMonthAndClosedTrue(monthStart)) {
            log.info("마감된 월은 집계하지 않습니다: {}", month);
            return CalculationResult.skipped(month, month + " 은(는) 마감된 월이라 재집계하지 않았습니다.");
        }
        List<String> warnings = new ArrayList<>();

        Map<Long, EmployeeMonthlyCost> costs = calculateEmployeeCosts(month, warnings);
        ProjectTotals totals = calculateProjects(month, costs, warnings);
        calculateCompanyCost(month, costs);

        log.info("월 손익 집계 완료: month={}, employees={}, projects={}, removed={}, warnings={}",
                month, costs.size(), totals.projectCount, totals.removedCount, warnings.size());
        return new CalculationResult(month, false, costs.size(), totals.projectCount, totals.removedCount,
                totals.revenue, totals.cost, List.copyOf(warnings));
    }

    /** 해당 월에 재직한 직원의 월 원가를 계산해 저장한다. */
    private Map<Long, EmployeeMonthlyCost> calculateEmployeeCosts(YearMonth month, List<String> warnings) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        Map<Long, EmployeeMonthlyCost> existing = monthlyCostRepository.findAllByTargetMonth(monthStart).stream()
                .collect(Collectors.toMap(EmployeeMonthlyCost::getEmployeeId, Function.identity(), (a, b) -> a));
        Map<EmployeeType, Optional<EmployeeCostPolicy>> policies = new HashMap<>();

        Map<Long, EmployeeMonthlyCost> result = new HashMap<>();
        for (Employee employee : employeeRepository.findAllByDeletedFalseOrderByNameAsc()) {
            if (!employedDuring(employee, monthStart, monthEnd)) {
                continue;
            }
            Optional<Payroll> payroll = payrollRepository.findEffective(employee.id(), monthEnd)
                    .or(() -> payrollRepository.findEffective(employee.id(), monthStart));
            if (payroll.isEmpty()) {
                warnings.add("급여 정보 없음: " + employee.getName() + " (id=" + employee.id() + ")");
                continue;
            }
            Optional<EmployeeCostPolicy> policy = policies.computeIfAbsent(employee.getType(),
                    type -> policyRepository.findFirstByTypeAndApplyYearLessThanEqualOrderByApplyYearDesc(type, month.getYear()));
            CostBreakdown breakdown;
            if (policy.isPresent()) {
                breakdown = policy.get().breakdown(payroll.get().monthlySalary());
            } else {
                warnings.add("원가 정책 없음: " + employee.getType().label() + " " + month.getYear() + "년 (기본급만 반영)");
                Money salary = payroll.get().monthlySalary();
                breakdown = new CostBreakdown(salary, Money.ZERO, Money.ZERO, salary);
            }
            EmployeeMonthlyCost cost = existing.remove(employee.id());
            if (cost == null) {
                cost = EmployeeMonthlyCost.create(employee.id(), month, breakdown);
            } else {
                cost.update(breakdown);
            }
            result.put(employee.id(), monthlyCostRepository.save(cost));
        }
        monthlyCostRepository.deleteAll(existing.values());
        return result;
    }

    private ProjectTotals calculateProjects(YearMonth month, Map<Long, EmployeeMonthlyCost> costs, List<String> warnings) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        Map<Long, List<ProjectRevenuePlan>> plansByProject = revenuePlanRepository.findIssuedBetween(monthStart, monthEnd).stream()
                .collect(Collectors.groupingBy(ProjectRevenuePlan::getProjectId));
        Map<Long, List<ProjectAssignment>> assignmentsByProject = assignmentRepository.findOverlapping(monthStart, monthEnd).stream()
                .collect(Collectors.groupingBy(ProjectAssignment::getProjectId));
        Map<Long, List<ProjectExpense>> expensesByProject = expenseRepository.findAllByExpenseDateBetween(monthStart, monthEnd).stream()
                .collect(Collectors.groupingBy(ProjectExpense::getProjectId));
        Map<Long, MonthlyRevenueSummary> existing = summaryRepository.findAllByTargetMonthOrderByProjectNameAsc(monthStart).stream()
                .collect(Collectors.toMap(MonthlyRevenueSummary::getProjectId, Function.identity(), (a, b) -> a));

        Set<Long> projectIds = new LinkedHashSet<>();
        projectRepository.findOverlapping(monthStart, monthEnd).forEach(p -> projectIds.add(p.id()));
        projectIds.addAll(plansByProject.keySet());
        projectIds.addAll(assignmentsByProject.keySet());
        projectIds.addAll(expensesByProject.keySet());

        // 삭제된 프로젝트는 조회되지 않으므로 자연스럽게 집계 대상에서 빠진다.
        Map<Long, Project> projects = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::id, Function.identity()));
        Map<Long, Department> departments = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::id, Function.identity()));

        ProjectTotals totals = new ProjectTotals();
        for (Project project : projects.values()) {
            Money revenue = plansByProject.getOrDefault(project.id(), List.of()).stream()
                    .map(ProjectRevenuePlan::getAmount)
                    .reduce(Money.ZERO, Money::plus);
            // 취소된 프로젝트는 계약금액을 다 받지 못하므로 진행 기준 대신 실제 청구액을 관리 매출로 본다.
            Money managedRevenue = project.getStatus() == ProjectStatus.CANCELLED ? revenue : project.managedRevenue(month);
            Money laborCost = Money.ZERO;
            for (ProjectAssignment assignment : assignmentsByProject.getOrDefault(project.id(), List.of())) {
                EmployeeMonthlyCost employeeCost = costs.get(assignment.getEmployeeId());
                if (employeeCost == null) {
                    warnings.add("투입 직원 원가 없음: 프로젝트 " + project.getCode() + ", 직원 id=" + assignment.getEmployeeId());
                    continue;
                }
                laborCost = laborCost.plus(employeeCost.getTotalCost().times(assignment.manMonth(month)));
            }
            Money directCost = expensesByProject.getOrDefault(project.id(), List.of()).stream()
                    .map(ProjectExpense::getAmount)
                    .reduce(Money.ZERO, Money::plus);
            Department lead = departments.get(project.getLeadDepartmentId());
            Snapshot snapshot = new Snapshot(project.getCode(), project.getName(), project.getLeadDepartmentId(),
                    lead == null ? "-" : lead.getName(), revenue, managedRevenue, laborCost, directCost);

            MonthlyRevenueSummary summary = existing.remove(project.id());
            if (summary == null) {
                summaryRepository.save(MonthlyRevenueSummary.create(project.id(), month, snapshot));
            } else {
                summary.update(snapshot);
            }
            totals.add(revenue, snapshot.cost());
        }
        // 더 이상 집계 대상이 아닌 프로젝트(삭제, 기간 변경 등)의 기존 집계는 제거한다.
        summaryRepository.deleteAll(existing.values());
        totals.removedCount = existing.size();
        return totals;
    }

    /**
     * 정직원 전체 원가 중 프로젝트에 배분된 금액과 배분되지 않은 금액을 집계한다.
     * 총원가는 투입 M/M과 같은 기준으로 재직일수만큼 일할한다. (월중 입사·퇴사)
     */
    private void calculateCompanyCost(YearMonth month, Map<Long, EmployeeMonthlyCost> costs) {
        LocalDate monthStart = month.atDay(1);
        List<Employee> fullTimers = employeeRepository.findAllByIdInAndDeletedFalse(costs.keySet()).stream()
                .filter(e -> e.getType() == EmployeeType.FULL_TIME)
                .toList();
        Set<Long> fullTimeIds = fullTimers.stream().map(Employee::id).collect(Collectors.toSet());

        Money total = fullTimers.stream()
                .map(e -> costs.get(e.id()).getTotalCost().times(employmentPeriod(e).manMonth(month)))
                .reduce(Money.ZERO, Money::plus);

        // 프로젝트 비용 산식과 동일하게 투입 M/M 만큼 배분된 것으로 본다. (삭제된 프로젝트의 투입은 제외)
        List<ProjectAssignment> assignments = assignmentRepository.findOverlapping(monthStart, month.atEndOfMonth());
        Set<Long> liveProjectIds = projectRepository.findAllById(
                        assignments.stream().map(ProjectAssignment::getProjectId).collect(Collectors.toSet()))
                .stream().map(Project::id).collect(Collectors.toSet());
        Money allocated = assignments.stream()
                .filter(a -> fullTimeIds.contains(a.getEmployeeId()) && liveProjectIds.contains(a.getProjectId()))
                .map(a -> costs.get(a.getEmployeeId()).getTotalCost().times(a.manMonth(month)))
                .reduce(Money.ZERO, Money::plus);

        companySummaryRepository.findByTargetMonth(monthStart).ifPresentOrElse(
                summary -> summary.update(total, allocated),
                () -> companySummaryRepository.save(CompanyMonthlyCostSummary.create(month, total, allocated)));
    }

    private static Period employmentPeriod(Employee employee) {
        return new Period(employee.getJoinDate(), employee.getResignationDate());
    }

    private static boolean employedDuring(Employee employee, LocalDate monthStart, LocalDate monthEnd) {
        if (employee.getJoinDate().isAfter(monthEnd)) {
            return false;
        }
        LocalDate resignationDate = employee.getResignationDate();
        return resignationDate == null || !resignationDate.isBefore(monthStart);
    }

    private static final class ProjectTotals {
        private Money revenue = Money.ZERO;
        private Money cost = Money.ZERO;
        private int projectCount;
        private int removedCount;

        void add(Money revenue, Money cost) {
            this.revenue = this.revenue.plus(revenue);
            this.cost = this.cost.plus(cost);
            this.projectCount++;
        }
    }

}
