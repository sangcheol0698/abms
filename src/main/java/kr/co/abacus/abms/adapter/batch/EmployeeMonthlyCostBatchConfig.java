package kr.co.abacus.abms.adapter.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kr.co.abacus.abms.application.employee.outbound.EmployeeCostPolicyRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeMonthlyCostRepository;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;
import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.shared.Money;
import lombok.RequiredArgsConstructor;

/**
 * 직원 월별 비용 계산 배치
 * - 매월 직원 한 명당 회사에 청구되는 총비용(기본급 + 제경비 + 판관비)을 미리 계산해서 담아두는 것
 * - 근무일자는 고려하지 않음 (중간에 퇴사, 입사하여도 기본 월을 기준으로 계산)
 */
@Configuration
@RequiredArgsConstructor
public class EmployeeMonthlyCostBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final PayrollRepository payrollRepository;
    private final EmployeeCostPolicyRepository policyRepository;
    private final EmployeeMonthlyCostRepository monthlyCostRepository;

    @Bean
    public Job employeeCostJob() {
        return new JobBuilder("employeeCostJob", jobRepository)
            .start(employeeCostStep())
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    public Step employeeCostStep() {
        return new StepBuilder("employeeCostStep", jobRepository)
            .<Employee, EmployeeMonthlyCost>chunk(100)
            .transactionManager(transactionManager)
            .reader(employeeReader())
            .processor(employeeCostProcessor(null))
            .writer(employeeCostWriter())
            .build();
    }


    @Bean
    @StepScope
    public JpaPagingItemReader<Employee> employeeReader() {
        return new JpaPagingItemReaderBuilder<Employee>()
            .name("employeeReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT e FROM Employee e WHERE e.status = :status ORDER BY e.id ASC")
            .parameterValues(Collections.singletonMap("status", EmployeeStatus.ACTIVE))
            .pageSize(100)
            .build();
    }
    @Bean
    @StepScope
    public ItemProcessor<Employee, EmployeeMonthlyCost> employeeCostProcessor(
        @Value("#{jobParameters['targetDate']}") String targetDateStr
    ) {
        return employee -> {
            LocalDate targetDate = (targetDateStr != null) ?
                LocalDate.parse(targetDateStr) : LocalDate.now().minusDays(1);

            String costMonth = targetDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
            int applyYear = targetDate.getYear();

            Payroll payroll = payrollRepository.findByEmployeeIdAndTargetDate(employee.getId(), targetDate)
                .orElseThrow(() -> new IllegalArgumentException("급여 정보 누락 - 직원(id=" + employee.getId() + "), 날짜(" + targetDate + ")"));

            EmployeeCostPolicy policy = policyRepository.findByApplyYearAndType(applyYear, employee.getType())
                .orElseThrow(() -> new IllegalArgumentException("비용 정책 누락: " + applyYear + ", " + employee.getType()));

            BigDecimal twelve = BigDecimal.valueOf(12);
            BigDecimal overheadRate = BigDecimal.valueOf(policy.getOverheadRate());
            BigDecimal sgaRate = BigDecimal.valueOf(policy.getSgaRate());

            // 비용 계산
            Money monthlySalaryCost = payroll.getAnnualSalary().divide(twelve);
            Money overheadCost = monthlySalaryCost.multiply(overheadRate);
            Money sgaCost = monthlySalaryCost.multiply(sgaRate);
            Money totalCost = monthlySalaryCost.add(overheadCost).add(sgaCost);

            // 멱등성 보장을 위한 기존 데이터 조회
            EmployeeMonthlyCost existingCost = monthlyCostRepository.findByEmployeeIdAndCostMonth(employee.getId(), costMonth).orElse(null);

            if (existingCost != null) {
                // 기존 데이터가 있다면, 값만 최신화 (Update)
                existingCost.update(monthlySalaryCost, overheadCost, sgaCost, totalCost);
                return existingCost;
            } else {
                // 기존 데이터가 있다면, 신규 생성 (Create)
                return EmployeeMonthlyCost.create(
                     employee.getId(),
                     costMonth,
                     monthlySalaryCost,
                     overheadCost,
                     sgaCost,
                     totalCost
                );
            }
        };
    }

    @Bean
    public ItemWriter<EmployeeMonthlyCost> employeeCostWriter() {
        return chunk -> monthlyCostRepository.saveAll(chunk.getItems());
    }
}
