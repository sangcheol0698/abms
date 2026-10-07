package kr.co.abacus.abms.project;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeService;

/**
 * 프로젝트 상세의 부분 갱신 영역(매출 계획, 직접비, 투입 인력) 뷰 모델 생성.
 */
@Component
public class ProjectSections {

    private final ProjectRevenueService revenueService;
    private final ProjectExpenseService expenseService;
    private final ProjectAssignmentService assignmentService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public ProjectSections(ProjectRevenueService revenueService, ProjectExpenseService expenseService,
                           ProjectAssignmentService assignmentService, EmployeeService employeeService,
                           DepartmentService departmentService) {
        this.revenueService = revenueService;
        this.expenseService = expenseService;
        this.assignmentService = assignmentService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    public RevenueSection revenue(Project project, boolean canWrite) {
        return new RevenueSection(project, revenueService.plans(project.id()), canWrite);
    }

    public ExpenseSection expense(Project project, boolean canWrite) {
        return new ExpenseSection(project, expenseService.expenses(project.id()), canWrite);
    }

    public StaffingSection staffing(Project project, boolean canWrite) {
        List<ProjectAssignment> assignments = assignmentService.assignments(project.id());
        Set<Long> employeeIds = assignments.stream().map(ProjectAssignment::getEmployeeId).collect(Collectors.toSet());
        Map<Long, Employee> employees = employeeService.findAll(employeeIds).stream()
                .collect(Collectors.toMap(Employee::id, Function.identity()));
        DepartmentTree tree = departmentService.tree();
        List<StaffRow> rows = assignments.stream()
                .map(a -> {
                    Employee e = employees.get(a.getEmployeeId());
                    return new StaffRow(a, e, e == null ? "-" : tree.nameOf(e.getDepartmentId()),
                            e != null && !e.getDepartmentId().equals(project.getLeadDepartmentId()));
                })
                .toList();
        return new StaffingSection(project, rows, canWrite);
    }

    public record RevenueSection(Project project, List<ProjectRevenuePlan> plans, boolean canWrite) {

        public Money planned() {
            return plans.stream().map(ProjectRevenuePlan::getAmount).reduce(Money.ZERO, Money::plus);
        }

        public Money issued() {
            return plans.stream().filter(ProjectRevenuePlan::isIssued).map(ProjectRevenuePlan::getAmount).reduce(Money.ZERO, Money::plus);
        }

        public Money unplanned() {
            return project.getContractAmount().minus(planned());
        }

        public int issuedPercent() {
            if (project.getContractAmount().amount().signum() == 0) {
                return 0;
            }
            return issued().amount().multiply(java.math.BigDecimal.valueOf(100))
                    .divide(project.getContractAmount().amount(), 0, java.math.RoundingMode.DOWN).intValue();
        }

    }

    public record ExpenseSection(Project project, List<ProjectExpense> expenses, boolean canWrite) {

        public Money total() {
            return expenses.stream().map(ProjectExpense::getAmount).reduce(Money.ZERO, Money::plus);
        }

        /** 분류별 합계 (금액이 큰 순) */
        public List<Map.Entry<ExpenseCategory, Money>> byCategory() {
            return expenses.stream()
                    .collect(Collectors.groupingBy(ProjectExpense::getCategory, () -> new java.util.EnumMap<>(ExpenseCategory.class),
                            Collectors.reducing(Money.ZERO, ProjectExpense::getAmount, Money::plus)))
                    .entrySet().stream()
                    .sorted(Map.Entry.<ExpenseCategory, Money>comparingByValue().reversed())
                    .toList();
        }

        /** 계약금액 대비 직접비 비율(%) */
        public int contractPercent() {
            if (project.getContractAmount().amount().signum() == 0) {
                return 0;
            }
            return total().amount().multiply(java.math.BigDecimal.valueOf(100))
                    .divide(project.getContractAmount().amount(), 0, java.math.RoundingMode.HALF_UP).intValue();
        }

    }

    /**
     * @param supporting 주관 부서가 아닌 부서에서 지원 투입된 인력인지
     */
    public record StaffRow(ProjectAssignment assignment, @Nullable Employee employee, String departmentName, boolean supporting) {
    }

    public record StaffingSection(Project project, List<StaffRow> rows, boolean canWrite) {

        public long activeCount() {
            java.time.LocalDate today = java.time.LocalDate.now();
            return rows.stream().filter(r -> r.assignment().isActiveOn(today)).count();
        }

    }

}
