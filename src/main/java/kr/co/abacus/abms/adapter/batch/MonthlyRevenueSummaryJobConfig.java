package kr.co.abacus.abms.adapter.batch;

import static reactor.netty.http.HttpConnectionLiveness.*;

import java.util.Arrays;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MonthlyRevenueSummaryJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job monthlyRevenueSummaryJob() {
        return new JobBuilder("monthlyRevenueSummaryJob", jobRepository)
            .start(monthlyRevenueSummaryStep())
            .build();
    }

    @Bean
    public Step monthlyRevenueSummaryStep() {
        return new StepBuilder("monthlyRevenueSummaryStep", jobRepository)
            .<String, String>chunk(2)
            .reader(monthlyRevenueSummaryReader())
            .processor(summaryProcessor())
            .writer(monthlyRevenueSummaryWriter())
            .build();
    }

    @Bean
    public ItemReader<String> monthlyRevenueSummaryReader() {
        return new ListItemReader<>(Arrays.asList(
            "가나다", "abc", "xyz", "123"
        ));
    }

    @Bean
    public ItemProcessor<String, String> summaryProcessor() {
        return item -> item.toUpperCase();
    }

    @Bean
    public ItemWriter<String> monthlyRevenueSummaryWriter() {
        return items -> {
            log.info(">>> 청크 쓰기 시작");
            for (String item : items) {
                log.info("결과 : {}", item);
            }
            log.info(">>> 청크 쓰기 완료");
        };
    }

}
