package kr.co.abacus.abms.adapter.batch;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import lombok.RequiredArgsConstructor;

// todo: 미리 계산된 인력 비용과 프로젝트를 참고하여 월별 프로젝트별 매출 계산
@Configuration
@RequiredArgsConstructor
public class RevenueMonthlySummaryBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ProjectRepository projectRepository;

}
