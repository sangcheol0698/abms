package kr.co.abacus.abms.adapter.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeMonthlyCostRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
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
    private final EntityManagerFactory entityManagerFactory;

    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final EmployeeMonthlyCostRepository employeeMonthlyCostRepository;
    private final MonthlyRevenueSummaryRepository summaryRepository;
    private final BatchObservabilityListener batchObservabilityListener;

    @Bean
    public Job revenueMonthlySummaryJob() {
        return new JobBuilder("revenueMonthlySummaryJob", jobRepository)
            .listener(batchObservabilityListener)
            .start(deleteMonthlyRevenueSummaryStep())
            .next(revenueMonthlySummaryStep())
            .build();
    }

    @Bean
    public Step deleteMonthlyRevenueSummaryStep() {
        return new StepBuilder("deleteMonthlyRevenueSummaryStep", jobRepository)
            .tasklet(deleteMonthlyRevenueSummaryTasklet(null), transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public Tasklet deleteMonthlyRevenueSummaryTasklet(
        @Value("#{jobParameters['targetDate'] ?: null}") LocalDate targetDate
    ) {
        return (contribution, chunkContext) -> {
            LocalDate executeDate = (targetDate != null) ? targetDate : LocalDate.now().minusDays(1);
            YearMonth yearMonth = YearMonth.from(executeDate);

            log.info("매출 요약 데이터 삭제 시작: {}", yearMonth);
            summaryRepository.deleteBySummaryDateBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step revenueMonthlySummaryStep() {
        return new StepBuilder("revenueMonthlySummaryStep", jobRepository)
            .<Project, MonthlyRevenueSummary>chunk(50)
            .transactionManager(transactionManager)
            .listener(batchObservabilityListener)
            .reader(projectReader(null))
            .processor(revenueMonthlySummaryProcessor(null))
            .writer(revenueMonthlySummaryWriter())
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Project> projectReader(
        @Value("#{jobParameters['targetDate'] ?: null}") LocalDate targetDate
    ) {
        LocalDate executeDate = (targetDate != null) ? targetDate : LocalDate.now().minusDays(1);

        // 1. 파라미터로 받은 targetDate를 기준으로 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.from(executeDate);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // 2. 쿼리에 넘길 파라미터 맵 세팅
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startOfMonth", startOfMonth);
        parameters.put("endOfMonth", endOfMonth);

        // 3. Reader 빌드 및 반환
        return new JpaPagingItemReaderBuilder<Project>()
            .name("projectReader")
            .entityManagerFactory(entityManagerFactory)
            // 🌟 요청하신 겹치는 기간 조회 쿼리 적용 (Paging을 위해 ORDER BY 추가 필수!)
            .queryString("SELECT p FROM Project p " +
                "WHERE p.deleted = false " + // 삭제되지 않은 프로젝트만
                "AND p.period.startDate <= :endOfMonth " +
                "AND p.period.endDate >= :startOfMonth " +
                "ORDER BY p.id ASC") // JpaPagingItemReader는 순서 보장을 위해 ORDER BY가 무조건 있어야 합니다.
            .parameterValues(parameters)
            .pageSize(50)
            .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Project, MonthlyRevenueSummary> revenueMonthlySummaryProcessor(
        @Value("#{jobParameters['targetDate'] ?: null}") LocalDate targetDate
    ) {
        return project -> {
            Department leadDepartment = departmentRepository.findByIdAndDeletedFalse(project.getLeadDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("주관 부서 누락: " + project.getLeadDepartmentId()));

            // 기준월 세팅
            LocalDate executeDate = (targetDate != null) ? targetDate : LocalDate.now().minusDays(1);
            YearMonth yearMonth = YearMonth.from(executeDate);
            LocalDate monthStart = yearMonth.atDay(1);  // 예: 2026-02-01
            LocalDate monthEnd = yearMonth.atEndOfMonth();         // 예: 2026-02-28
            String costMonth = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")); // "2026-02"

            // ① 매출(Revenue) 집계
            Money totalRevenue = revenuePlanRepository.findByProjectIdAndRevenueDateBetweenAndIsIssuedTrue(project.getId(), monthStart, monthEnd)
                .stream()
                .map(ProjectRevenuePlan::getAmount)
                .reduce(Money.zero(), Money::add);

            // ② 비용(Cost) 집계
            Money totalCost = Money.zero();
            List<ProjectAssignment> assignments = assignmentRepository.findOverlappingAssignments(project.getId(), monthStart, monthEnd);

            for (ProjectAssignment assign : assignments) {
                BigDecimal mm = assign.calculateManMonth(executeDate);

                EmployeeMonthlyCost empCost = employeeMonthlyCostRepository
                    .findByEmployeeIdAndCostMonth(assign.getEmployeeId(), costMonth)
                    .orElseThrow(() -> new IllegalArgumentException("직원 비용 누락 - 직원(id=" + assign.getEmployeeId() + "), 월(costMonth=" + costMonth + "): "));

                totalCost = totalCost.add(empCost.getTotalCost().multiply(mm));
            }

            // ③ 이익 계산
            Money totalProfit = totalRevenue.subtract(totalCost);

            // ④ 결과 반환
           return MonthlyRevenueSummary.create(
               new MonthlyRevenueSummaryCreateRequest(
                   project.getId(),
                   project.getCode(),
                   project.getName(),
                   leadDepartment.getId(),
                   leadDepartment.getCode(),
                   leadDepartment.getName(),
                   executeDate, // monthStart 대신 다시 executeDate 사용
                   totalRevenue,
                   totalCost,
                   totalProfit
               )
           );
        };
    }

    @Bean
    public ItemWriter<MonthlyRevenueSummary> revenueMonthlySummaryWriter() {
        return chunk -> summaryRepository.saveAll(chunk.getItems());
    }

}
