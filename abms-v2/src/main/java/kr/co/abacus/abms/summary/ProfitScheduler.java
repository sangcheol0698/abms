package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 전월·당월 손익을 재집계한다. (마감된 월은 건너뜀)
 */
@Component
public class ProfitScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProfitScheduler.class);
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ProfitCalculationService calculationService;
    private final SummaryProperties properties;

    public ProfitScheduler(ProfitCalculationService calculationService, SummaryProperties properties) {
        this.calculationService = calculationService;
        this.properties = properties;
    }

    @Scheduled(cron = "${abms.summary.cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void recalculate() {
        if (!properties.scheduleEnabled()) {
            return;
        }
        YearMonth current = YearMonth.from(LocalDate.now(SEOUL));
        for (YearMonth month : new YearMonth[]{current.minusMonths(1), current}) {
            try {
                CalculationResult result = calculationService.calculate(month);
                if (!result.warnings().isEmpty()) {
                    log.warn("월 손익 집계 경고 {}건 ({}): {}", result.warnings().size(), month, result.warnings());
                }
            } catch (RuntimeException e) {
                log.error("월 손익 정기 집계 실패: {}", month, e);
            }
        }
    }

}
