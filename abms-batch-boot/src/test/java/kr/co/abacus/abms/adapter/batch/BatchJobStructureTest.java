package kr.co.abacus.abms.adapter.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import io.micrometer.core.instrument.MeterRegistry;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetLinkSender;
import kr.co.abacus.abms.AbmsBatchApplication;
import kr.co.abacus.abms.application.auth.outbound.RegistrationLinkSender;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeCostPolicyRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

@ActiveProfiles("test")
@SpringBootTest(
        classes = AbmsBatchApplication.class,
        properties = {
                "spring.batch.job.enabled=false",
                "spring.sql.init.mode=never",
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.ai.openai.api-key=test"
        }
)
@DisplayName("배치 현재 구조 테스트")
class BatchJobStructureTest {

    @MockitoBean
    private RegistrationLinkSender registrationLinkSender;

    @MockitoBean
    private PasswordResetLinkSender passwordResetLinkSender;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("employeeCostJob")
    private Job employeeCostJob;

    @Autowired
    @Qualifier("revenueMonthlySummaryJob")
    private Job revenueMonthlySummaryJob;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeCostPolicyRepository employeeCostPolicyRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private kr.co.abacus.abms.adapter.infrastructure.employee.EmployeeMonthlyCostRepository employeeMonthlyCostRepository;

    @Autowired
    private kr.co.abacus.abms.adapter.infrastructure.summary.MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("employeeCostJob는 활성 직원의 월 비용을 계산해 저장한다")
    void employeeCostJob_calculatesMonthlyCost() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 2, 15);
        Department department = createDepartment("원가팀A");
        Employee employee = createEmployee(department, "원가직원A", "batch-cost-a@abms.co.kr");
        ensurePoliciesForAllEmployeeTypes(2026, 0.1, 0.2);
        ensurePayrollForActiveEmployees(targetDate, 120_000_000L);
        flushAndClear();

        JobExecution execution = jobLauncher.run(employeeCostJob, jobParameters(targetDate, 1L));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        EmployeeMonthlyCost monthlyCost = employeeMonthlyCostRepository
                .findByEmployeeIdAndCostMonth(employee.getIdOrThrow(), "202602")
                .orElseThrow();

        assertThat(monthlyCost.getMonthlySalary().amount().longValue()).isEqualTo(10_000_000L);
        assertThat(monthlyCost.getOverHeadCost().amount().longValue()).isEqualTo(1_000_000L);
        assertThat(monthlyCost.getSgaCost().amount().longValue()).isEqualTo(2_000_000L);
        assertThat(monthlyCost.getTotalCost().amount().longValue()).isEqualTo(13_000_000L);
        assertThat(meterRegistry.find("abms.batch.job.executions.total")
                .tags("job", "employeeCostJob", "status", "COMPLETED")
                .counter()).isNotNull();
        assertThat(meterRegistry.find("abms.batch.step.duration")
                .tags("job", "employeeCostJob", "step", "employeeCostStep", "status", "COMPLETED")
                .timer()).isNotNull();
    }

    @Test
    @DisplayName("revenueMonthlySummaryJob는 발행 매출과 월 비용으로 손익을 집계한다")
    void revenueMonthlySummaryJob_aggregatesIssuedRevenueAndCost() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 2, 15);
        Department leadDepartment = createDepartment("집계팀A");
        Employee employee = createEmployee(leadDepartment, "집계직원A", "batch-summary-a@abms.co.kr");
        Party party = partyRepository.save(Party.create(new PartyCreateRequest("집계협력사A", null, null, null, null)));
        Project project = projectRepository.save(Project.create(
                party.getIdOrThrow(),
                leadDepartment.getIdOrThrow(),
                "BATCH-SUM-001",
                "배치 집계 프로젝트 A",
                null,
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        ));

        ProjectRevenuePlan issuedPlan = ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 2, 20),
                RevenueType.INTERMEDIATE_PAYMENT,
                30_000_000L,
                "2월 발행"
        ));
        issuedPlan.issue();
        projectRevenuePlanRepository.save(issuedPlan);

        projectRevenuePlanRepository.save(ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                2,
                LocalDate.of(2026, 2, 25),
                RevenueType.BALANCE_PAYMENT,
                50_000_000L,
                "미발행 제외"
        )));

        projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getIdOrThrow(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        )));

        employeeMonthlyCostRepository.save(EmployeeMonthlyCost.create(
                employee.getIdOrThrow(),
                "202602",
                Money.wons(10_000_000L),
                Money.wons(1_000_000L),
                Money.wons(2_000_000L),
                Money.wons(13_000_000L)
        ));
        flushAndClear();

        JobExecution execution = jobLauncher.run(revenueMonthlySummaryJob, jobParameters(targetDate, 2L));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<MonthlyRevenueSummary> summaries = monthlyRevenueSummaryRepository.findAll().stream()
                .filter(summary -> summary.getProjectId().equals(project.getIdOrThrow()))
                .filter(summary -> summary.getSummaryDate().isEqual(targetDate))
                .toList();

        assertThat(summaries).hasSize(1);
        MonthlyRevenueSummary summary = summaries.getFirst();
        assertThat(summary.getRevenueAmount().amount().longValue()).isEqualTo(30_000_000L);
        assertThat(summary.getCostAmount().amount().longValue()).isEqualTo(13_000_000L);
        assertThat(summary.getProfitAmount().amount().longValue()).isEqualTo(17_000_000L);
        assertThat(meterRegistry.find("abms.batch.job.executions.total")
                .tags("job", "revenueMonthlySummaryJob", "status", "COMPLETED")
                .counter()).isNotNull();
        assertThat(meterRegistry.find("abms.batch.step.read.count")
                .tags("job", "revenueMonthlySummaryJob", "step", "revenueMonthlySummaryStep")
                .summary()).isNotNull();
    }

    @Test
    @DisplayName("revenueMonthlySummaryJob는 같은 월 재실행 시 기존 요약을 삭제하고 다시 적재하여 멱등성을 보장한다")
    void revenueMonthlySummaryJob_isIdempotentOnRerun() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 2, 15);
        Department leadDepartment = createDepartment("집계팀B");
        Employee employee = createEmployee(leadDepartment, "집계직원B", "batch-summary-b@abms.co.kr");
        Party party = partyRepository.save(Party.create(new PartyCreateRequest("집계협력사B", null, null, null, null)));
        Project project = projectRepository.save(Project.create(
                party.getIdOrThrow(),
                leadDepartment.getIdOrThrow(),
                "BATCH-SUM-002",
                "배치 집계 프로젝트 B",
                null,
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        ));

        ProjectRevenuePlan issuedPlan = ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 2, 10),
                RevenueType.DOWN_PAYMENT,
                20_000_000L,
                "중복 적재 확인"
        ));
        issuedPlan.issue();
        projectRevenuePlanRepository.save(issuedPlan);

        projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getIdOrThrow(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        )));

        employeeMonthlyCostRepository.save(EmployeeMonthlyCost.create(
                employee.getIdOrThrow(),
                "202602",
                Money.wons(8_000_000L),
                Money.wons(800_000L),
                Money.wons(1_600_000L),
                Money.wons(10_400_000L)
        ));
        flushAndClear();

        jobLauncher.run(revenueMonthlySummaryJob, jobParameters(targetDate, 3L));
        jobLauncher.run(revenueMonthlySummaryJob, jobParameters(targetDate, 4L));

        List<MonthlyRevenueSummary> summaries = monthlyRevenueSummaryRepository.findAll().stream()
                .filter(summary -> summary.getProjectId().equals(project.getIdOrThrow()))
                .filter(summary -> summary.getSummaryDate().isEqual(targetDate))
                .toList();

        assertThat(summaries).hasSize(1);
    }

    private JobParameters jobParameters(LocalDate targetDate, long runId) {
        return new JobParametersBuilder()
                .addString("targetDate", targetDate.toString())
                .addLong("run.id", runId)
                .toJobParameters();
    }

    private Department createDepartment(String name) {
        return departmentRepository.save(Department.create(
                "DEPT-" + System.nanoTime(),
                name,
                DepartmentType.TEAM,
                null,
                null
        ));
    }

    private Employee createEmployee(Department department, String name, String email) {
        return employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                name,
                email,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
    }

    private void ensurePoliciesForAllEmployeeTypes(int applyYear, double overheadRate, double sgaRate) {
        for (EmployeeType type : EmployeeType.values()) {
            if (employeeCostPolicyRepository.findByApplyYearAndType(applyYear, type).isEmpty()) {
                employeeCostPolicyRepository.save(EmployeeCostPolicy.create(applyYear, type, overheadRate, sgaRate));
            }
        }
    }

    private void ensurePayrollForActiveEmployees(LocalDate targetDate, long annualSalary) {
        TypedQuery<Employee> query = entityManager.createQuery(
                "SELECT e FROM Employee e WHERE e.status = :status",
                Employee.class
        );
        List<Employee> activeEmployees = query.setParameter("status", EmployeeStatus.ACTIVE).getResultList();

        for (Employee activeEmployee : activeEmployees) {
            if (payrollRepository.findByEmployeeIdAndTargetDate(activeEmployee.getIdOrThrow(), targetDate).isEmpty()) {
                payrollRepository.save(Payroll.create(
                        activeEmployee.getIdOrThrow(),
                        Money.wons(annualSalary),
                        targetDate.withDayOfMonth(1)
                ));
            }
        }
    }

    private void flushAndClear() {
        entityManager.clear();
    }
}
