package kr.co.abacus.abms.adapter.batch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

@Component
public class BatchObservabilityListener implements JobExecutionListener, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger("batch-event");

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicLong> lastSuccessEpochMs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> lastFailureEpochMs = new ConcurrentHashMap<>();

    public BatchObservabilityListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        registerGauges(jobName);
        log.atInfo()
                .addKeyValue("event.category", "batch")
                .addKeyValue("event.action", "job_start")
                .addKeyValue("jobName", jobName)
                .addKeyValue("jobInstanceId", jobExecution.getJobInstance().getInstanceId())
                .addKeyValue("jobExecutionId", jobExecution.getId())
                .addKeyValue("parameters", jobExecution.getJobParameters().toString())
                .log("batch_job_start");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().name();
        long durationMs = durationMillis(jobExecution.getStartTime(), jobExecution.getEndTime());

        meterRegistry.counter("abms.batch.job.executions.total", "job", jobName, "status", status).increment();
        Timer.builder("abms.batch.job.duration")
                .tags(Tags.of("job", jobName, "status", status))
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs));

        if ("COMPLETED".equals(status)) {
            lastSuccessEpochMs.get(jobName).set(System.currentTimeMillis());
        } else {
            lastFailureEpochMs.get(jobName).set(System.currentTimeMillis());
        }

        var builder = log.atInfo()
                .addKeyValue("event.category", "batch")
                .addKeyValue("event.action", "job_finish")
                .addKeyValue("jobName", jobName)
                .addKeyValue("jobInstanceId", jobExecution.getJobInstance().getInstanceId())
                .addKeyValue("jobExecutionId", jobExecution.getId())
                .addKeyValue("status", status)
                .addKeyValue("durationMs", durationMs);
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            builder.addKeyValue("failureReason", jobExecution.getAllFailureExceptions().getFirst().getClass().getSimpleName());
        }
        builder.log("batch_job_finish");
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.atInfo()
                .addKeyValue("event.category", "batch")
                .addKeyValue("event.action", "step_start")
                .addKeyValue("jobName", stepExecution.getJobExecution().getJobInstance().getJobName())
                .addKeyValue("stepName", stepExecution.getStepName())
                .addKeyValue("jobExecutionId", stepExecution.getJobExecutionId())
                .log("batch_step_start");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();
        String status = stepExecution.getStatus().name();
        long durationMs = durationMillis(stepExecution.getStartTime(), stepExecution.getEndTime());

        meterRegistry.counter("abms.batch.step.executions.total", "job", jobName, "step", stepName, "status", status)
                .increment();
        meterRegistry.summary("abms.batch.step.read.count", "job", jobName, "step", stepName)
                .record(stepExecution.getReadCount());
        meterRegistry.summary("abms.batch.step.write.count", "job", jobName, "step", stepName)
                .record(stepExecution.getWriteCount());
        meterRegistry.summary("abms.batch.step.skip.count", "job", jobName, "step", stepName)
                .record(stepExecution.getSkipCount());
        Timer.builder("abms.batch.step.duration")
                .tags(Tags.of("job", jobName, "step", stepName, "status", status))
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs));

        log.atInfo()
                .addKeyValue("event.category", "batch")
                .addKeyValue("event.action", "step_finish")
                .addKeyValue("jobName", jobName)
                .addKeyValue("stepName", stepName)
                .addKeyValue("jobExecutionId", stepExecution.getJobExecutionId())
                .addKeyValue("status", status)
                .addKeyValue("readCount", stepExecution.getReadCount())
                .addKeyValue("writeCount", stepExecution.getWriteCount())
                .addKeyValue("skipCount", stepExecution.getSkipCount())
                .addKeyValue("durationMs", durationMs)
                .log("batch_step_finish");
        return stepExecution.getExitStatus();
    }

    private void registerGauges(String jobName) {
        lastSuccessEpochMs.computeIfAbsent(jobName, key -> {
            AtomicLong gauge = new AtomicLong(0L);
            Gauge.builder("abms.batch.job.last.success.epoch", gauge, AtomicLong::get)
                    .tag("job", key)
                    .register(meterRegistry);
            return gauge;
        });
        lastFailureEpochMs.computeIfAbsent(jobName, key -> {
            AtomicLong gauge = new AtomicLong(0L);
            Gauge.builder("abms.batch.job.last.failure.epoch", gauge, AtomicLong::get)
                    .tag("job", key)
                    .register(meterRegistry);
            return gauge;
        });
    }

    private long durationMillis(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }
        return Duration.between(start, end).toMillis();
    }
}
