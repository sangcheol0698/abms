package kr.co.abacus.abms.summary;

import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * 데모 환경 기동 시 손익 집계가 비어 있으면 최근 15개월을 집계해 화면에 바로 데이터가 보이게 한다.
 */
@Component
@ConditionalOnBooleanProperty("abms.demo.calculate-on-startup")
public class DemoSummaryInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSummaryInitializer.class);

    private final ProfitCalculationService calculationService;
    private final MonthlyRevenueSummaryRepository summaryRepository;

    public DemoSummaryInitializer(ProfitCalculationService calculationService, MonthlyRevenueSummaryRepository summaryRepository) {
        this.calculationService = calculationService;
        this.summaryRepository = summaryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (summaryRepository.count() > 0) {
            return;
        }
        YearMonth current = YearMonth.now();
        for (YearMonth month = current.minusMonths(15); !month.isAfter(current); month = month.plusMonths(1)) {
            calculationService.calculate(month);
        }
        log.info("데모 손익 집계를 생성했습니다.");
    }

}
