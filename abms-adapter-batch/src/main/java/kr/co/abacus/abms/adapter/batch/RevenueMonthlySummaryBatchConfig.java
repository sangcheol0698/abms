package kr.co.abacus.abms.adapter.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeMonthlyCostRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.application.summary.outbound.CompanyMonthlyCostSummaryRepository;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.application.summary.outbound.RevenueMonthClosingRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
import kr.co.abacus.abms.domain.summary.RevenueMonthClosingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로젝트별 월별 재무 집계
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RevenueMonthlySummaryBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final EmployeeMonthlyCostRepository employeeMonthlyCostRepository;
    private final MonthlyRevenueSummaryRepository summaryRepository;
    private final CompanyMonthlyCostSummaryRepository companyMonthlyCostSummaryRepository;
    private final RevenueMonthClosingRepository revenueMonthClosingRepository;
    private final BatchObservabilityListener batchObservabilityListener;

    @Bean
    public Job revenueMonthlySummaryJob() {
        return new JobBuilder("revenueMonthlySummaryJob", jobRepository)
            .listener(batchObservabilityListener)
            .start(revenueMonthlySummaryStep())
            .build();
    }

    @Bean
    public Step revenueMonthlySummaryStep() {
        return new StepBuilder("revenueMonthlySummaryStep", jobRepository)
            .listener(batchObservabilityListener)
            .tasklet(revenueMonthlySummaryTasklet(null, null), transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public Tasklet revenueMonthlySummaryTasklet(
        @Value("#{jobParameters['targetMonth'] ?: null}") String targetMonthParameter,
        @Value("#{jobParameters['targetDate'] ?: null}") String targetDateParameter
    ) {
        return (contribution, chunkContext) -> {
            YearMonth targetMonth = resolveTargetMonth(targetMonthParameter, targetDateParameter);
            LocalDate monthStart = targetMonth.atDay(1);
            LocalDate monthEnd = targetMonth.atEndOfMonth();
            String costMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

            if (revenueMonthClosingRepository.existsByTargetMonthAndStatusAndDeletedFalse(
                    monthStart,
                    RevenueMonthClosingStatus.CLOSED
            )) {
                log.info("마감된 매출 집계 월은 자동 재계산을 건너뜁니다: {}", targetMonth);
                return RepeatStatus.FINISHED;
            }

            List<Project> projects = findAffectedProjects(monthStart, monthEnd);
            for (Project project : projects) {
                reconcileProjectSummary(project, monthStart, monthEnd, costMonth);
            }
            reconcileCompanyMonthlyCostSummary(monthStart, monthEnd, costMonth);

            return RepeatStatus.FINISHED;
        };
    }

    private YearMonth resolveTargetMonth(String targetMonthParameter, String targetDateParameter) {
        if (targetMonthParameter != null && !targetMonthParameter.isBlank()) {
            return YearMonth.parse(targetMonthParameter, DateTimeFormatter.ofPattern("yyyyMM"));
        }
        LocalDate targetDate = targetDateParameter != null && !targetDateParameter.isBlank()
                ? LocalDate.parse(targetDateParameter)
                : LocalDate.now().minusDays(1);
        return YearMonth.from(targetDate);
    }

    private List<Project> findAffectedProjects(LocalDate monthStart, LocalDate monthEnd) {
        Set<Long> projectIds = new LinkedHashSet<>();

        projectRepository.findActiveProjects(monthStart, monthEnd).stream()
                .map(Project::getIdOrThrow)
                .forEach(projectIds::add);
        revenuePlanRepository.findByRevenueDateBetweenAndIsIssuedTrueAndDeletedFalse(monthStart, monthEnd).stream()
                .map(ProjectRevenuePlan::getProjectId)
                .forEach(projectIds::add);
        assignmentRepository.findActiveAssignments(monthStart, monthEnd).stream()
                .map(ProjectAssignment::getProjectId)
                .forEach(projectIds::add);
        summaryRepository.findAllByTargetMonthAndDeletedFalseOrderByProjectIdAsc(monthStart).stream()
                .map(MonthlyRevenueSummary::getProjectId)
                .forEach(projectIds::add);

        Map<Long, Project> projectsById = projectRepository.findAllByIdIn(projectIds).stream()
                .collect(Collectors.toMap(Project::getIdOrThrow, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        return projectIds.stream()
                .map(projectsById::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private void reconcileProjectSummary(Project project, LocalDate monthStart, LocalDate monthEnd, String costMonth) {
        Long projectId = project.getIdOrThrow();
        java.util.Optional<MonthlyRevenueSummary> existingSummary = summaryRepository
                .findByProjectIdAndTargetMonthAndDeletedFalse(projectId, monthStart);
        if (project.isDeleted()) {
            existingSummary.ifPresent(summaryRepository::delete);
            return;
        }

        List<ProjectRevenuePlan> issuedPlans = revenuePlanRepository
                .findByProjectIdAndRevenueDateBetweenAndIsIssuedTrueAndDeletedFalse(projectId, monthStart, monthEnd);
        List<ProjectAssignment> assignments = assignmentRepository.findOverlappingAssignments(projectId, monthStart, monthEnd);
        boolean overlapsProjectPeriod = overlaps(project.getPeriod().startDate(), project.getPeriod().endDate(), monthStart, monthEnd);

        if (!overlapsProjectPeriod && issuedPlans.isEmpty() && assignments.isEmpty()) {
            existingSummary.ifPresent(summaryRepository::delete);
            return;
        }

        Department leadDepartment = departmentRepository.findByIdAndDeletedFalse(project.getLeadDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("주관 부서 누락: " + project.getLeadDepartmentId()));

        Money totalRevenue = issuedPlans.stream()
                .map(ProjectRevenuePlan::getAmount)
                .reduce(Money.zero(), Money::add);
        Money totalCost = calculateCost(assignments, monthStart, costMonth);
        Money totalProfit = totalRevenue.subtract(totalCost);

        MonthlyRevenueSummaryCreateRequest request = new MonthlyRevenueSummaryCreateRequest(
                projectId,
                project.getCode(),
                project.getName(),
                leadDepartment.getIdOrThrow(),
                leadDepartment.getCode(),
                leadDepartment.getName(),
                monthStart,
                totalRevenue,
                totalCost,
                totalProfit
        );

        MonthlyRevenueSummary summary = existingSummary.orElseGet(() -> MonthlyRevenueSummary.create(request));
        if (existingSummary.isPresent()) {
            summary.update(request);
        }
        summaryRepository.saveAll(List.of(summary));
    }

    private Money calculateCost(List<ProjectAssignment> assignments, LocalDate targetMonth, String costMonth) {
        Money totalCost = Money.zero();
        for (ProjectAssignment assignment : assignments) {
            BigDecimal mm = assignment.calculateManMonth(targetMonth);
            EmployeeMonthlyCost empCost = employeeMonthlyCostRepository
                    .findByEmployeeIdAndCostMonth(assignment.getEmployeeId(), costMonth)
                    .orElseThrow(() -> new IllegalArgumentException("직원 비용 누락 - 직원(id=" + assignment.getEmployeeId() + "), 월(costMonth=" + costMonth + "): "));

            totalCost = totalCost.add(empCost.getTotalCost().multiply(mm));
        }
        return totalCost;
    }

    private void reconcileCompanyMonthlyCostSummary(LocalDate monthStart, LocalDate monthEnd, String costMonth) {
        List<EmployeeMonthlyCost> monthlyCosts = employeeMonthlyCostRepository.findAllByCostMonthAndDeletedFalse(costMonth);
        List<ProjectAssignment> assignments = assignmentRepository.findActiveAssignments(monthStart, monthEnd);
        Set<Long> employeeIds = new LinkedHashSet<>();
        monthlyCosts.stream()
                .map(EmployeeMonthlyCost::getEmployeeId)
                .forEach(employeeIds::add);
        assignments.stream()
                .map(ProjectAssignment::getEmployeeId)
                .forEach(employeeIds::add);

        Map<Long, Employee> employeesById = employeeIds.isEmpty()
                ? Map.of()
                : employeeRepository.findAllByIdInAndDeletedFalse(employeeIds.stream().toList()).stream()
                .collect(Collectors.toMap(Employee::getIdOrThrow, Function.identity()));
        Set<Long> fullTimeEmployeeIds = employeesById.values().stream()
                .filter(employee -> employee.getType() == EmployeeType.FULL_TIME)
                .map(Employee::getIdOrThrow)
                .collect(Collectors.toSet());
        Map<Long, EmployeeMonthlyCost> monthlyCostsByEmployeeId = monthlyCosts.stream()
                .collect(Collectors.toMap(EmployeeMonthlyCost::getEmployeeId, Function.identity(), (left, right) -> left));

        Money totalFullTimeEmployeeCost = monthlyCosts.stream()
                .filter(cost -> fullTimeEmployeeIds.contains(cost.getEmployeeId()))
                .map(EmployeeMonthlyCost::getTotalCost)
                .reduce(Money.zero(), Money::add);
        Money allocatedFullTimeEmployeeCost = calculateAllocatedFullTimeEmployeeCost(
                assignments,
                monthStart,
                fullTimeEmployeeIds,
                monthlyCostsByEmployeeId
        );
        Money unallocatedFullTimeEmployeeCost = totalFullTimeEmployeeCost.subtract(allocatedFullTimeEmployeeCost);

        CompanyMonthlyCostSummary summary = companyMonthlyCostSummaryRepository
                .findByTargetMonthAndDeletedFalse(monthStart)
                .orElseGet(() -> CompanyMonthlyCostSummary.create(
                        monthStart,
                        totalFullTimeEmployeeCost,
                        allocatedFullTimeEmployeeCost,
                        unallocatedFullTimeEmployeeCost
                ));
        summary.update(totalFullTimeEmployeeCost, allocatedFullTimeEmployeeCost, unallocatedFullTimeEmployeeCost);
        companyMonthlyCostSummaryRepository.saveAll(List.of(summary));
    }

    private Money calculateAllocatedFullTimeEmployeeCost(
            List<ProjectAssignment> assignments,
            LocalDate targetMonth,
            Set<Long> fullTimeEmployeeIds,
            Map<Long, EmployeeMonthlyCost> monthlyCostsByEmployeeId
    ) {
        Money allocatedCost = Money.zero();
        for (ProjectAssignment assignment : assignments) {
            Long employeeId = assignment.getEmployeeId();
            if (!fullTimeEmployeeIds.contains(employeeId)) {
                continue;
            }
            EmployeeMonthlyCost empCost = java.util.Optional.ofNullable(monthlyCostsByEmployeeId.get(employeeId))
                    .orElseThrow(() -> new IllegalArgumentException("직원 비용 누락 - 직원(id=" + employeeId + "), 월(targetMonth=" + targetMonth + "): "));
            BigDecimal mm = assignment.calculateManMonth(targetMonth);
            allocatedCost = allocatedCost.add(empCost.getTotalCost().multiply(mm));
        }
        return allocatedCost;
    }

    private boolean overlaps(LocalDate startDate, LocalDate endDate, LocalDate monthStart, LocalDate monthEnd) {
        LocalDate normalizedEndDate = endDate != null ? endDate : LocalDate.MAX;
        return !startDate.isAfter(monthEnd) && !normalizedEndDate.isBefore(monthStart);
    }

}
