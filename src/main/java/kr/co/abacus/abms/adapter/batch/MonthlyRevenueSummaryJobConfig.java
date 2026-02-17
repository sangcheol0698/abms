package kr.co.abacus.abms.adapter.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryManager;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlyRevenueSummaryJobConfig {

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MonthlyRevenueSummaryManager monthlyRevenueSummaryManager;

    @Bean
    public Job monthlyRevenueSummaryJob() {
        return new JobBuilder("monthlyRevenueSummaryJob", jobRepository)
            .start(monthlyRevenueSummaryStep())
            .build();
    }

    @Bean
    public Step monthlyRevenueSummaryStep() {
        return new StepBuilder("monthlyRevenueSummaryStep", jobRepository)
            .<Project, MonthlyRevenueSummary>chunk(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(activeProjectReader(null))
            .processor(summaryProcessor(null))
            .writer(summaryWriter())
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Project> activeProjectReader(
        @Value("#{jobParameters[targetMonth]}") String targetMonthStr
    ) {
        LocalDate targetDate = (targetMonthStr != null) ?
            LocalDate.parse(targetMonthStr, DateTimeFormatter.ISO_DATE) : LocalDate.now();

        LocalDate monthStart = targetDate.withDayOfMonth(1);
        LocalDate monthEnd = targetDate.withDayOfMonth(targetDate.lengthOfMonth());

        Map<String, Object> params = new HashMap<>();
        params.put("monthStart", monthStart);
        params.put("monthEnd", monthEnd);

        return new JpaPagingItemReaderBuilder<Project>()
            .name("activeProjectReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(CHUNK_SIZE)
            // 진행 중인 프로젝트 조건 (시작일 <= 월말 AND 종료일 >= 월초)
            .queryString("SELECT p FROM Project p " +
                "WHERE p.period.startDate <= :monthEnd " +
                "AND p.period.endDate >= :monthStart " +
                "ORDER BY p.id ASC")
            .parameterValues(params)
            .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Project, MonthlyRevenueSummary> summaryProcessor(
        @Value("#{jobParameters[targetMonth]}") String targetMonthStr
    ) {
        return project -> {
            log.info(">>> Processor 진입! Project ID: {}", project.getId());

            LocalDate targetDate = (targetMonthStr != null) ?
                LocalDate.parse(targetMonthStr, DateTimeFormatter.ISO_DATE) : LocalDate.now();

            return monthlyRevenueSummaryManager.calculateSummaryForProject(project, targetDate);
        };
    }

    @Bean
    public JpaItemWriter<MonthlyRevenueSummary> summaryWriter() {
        return new JpaItemWriterBuilder<MonthlyRevenueSummary>()
            .entityManagerFactory(entityManagerFactory)
            .build();
    }

}
