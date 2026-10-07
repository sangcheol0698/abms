package kr.co.abacus.abms.summary;

import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 손익 집계 월 마감/재오픈 및 수동 재집계.
 */
@Service
@Transactional
public class MonthClosingService {

    private final RevenueMonthClosingRepository closingRepository;
    private final ProfitCalculationService calculationService;
    private final AccessService accessService;

    public MonthClosingService(RevenueMonthClosingRepository closingRepository,
                               ProfitCalculationService calculationService, AccessService accessService) {
        this.closingRepository = closingRepository;
        this.calculationService = calculationService;
        this.accessService = accessService;
    }

    public CalculationResult recalculate(LoginUser user, YearMonth month) {
        accessService.require(user, PermissionCode.SUMMARY_MANAGE);
        if (month.isAfter(YearMonth.now())) {
            throw new BusinessException("미래 월은 집계할 수 없습니다.");
        }
        return calculationService.calculate(month);
    }

    /**
     * 한 해를 1월부터 이번 달(지난 해면 12월)까지 다시 집계한다. 마감된 월은 건너뛴다.
     * 집계 방식이 바뀐 뒤(예: 진행 기준 매출 추가) 지난 달들을 한 번에 맞출 때 쓴다.
     */
    public YearCalculationResult recalculateYear(LoginUser user, int year) {
        accessService.require(user, PermissionCode.SUMMARY_MANAGE);
        YearMonth now = YearMonth.now();
        if (year > now.getYear()) {
            throw new BusinessException("미래 연도는 집계할 수 없습니다.");
        }
        YearMonth last = year == now.getYear() ? now : YearMonth.of(year, 12);
        java.util.List<CalculationResult> results = new java.util.ArrayList<>();
        for (YearMonth month = YearMonth.of(year, 1); !month.isAfter(last); month = month.plusMonths(1)) {
            results.add(calculationService.calculate(month));
        }
        return new YearCalculationResult(year, results);
    }

    /** 최신 값으로 집계한 뒤 마감한다. */
    public void close(LoginUser user, YearMonth month) {
        accessService.require(user, PermissionCode.SUMMARY_MANAGE);
        if (!month.isBefore(YearMonth.now())) {
            throw new BusinessException("지난 달까지만 마감할 수 있습니다.");
        }
        RevenueMonthClosing closing = closingRepository.findByTargetMonth(month.atDay(1))
                .orElseGet(() -> closingRepository.save(RevenueMonthClosing.of(month)));
        if (!closing.isClosed()) {
            calculationService.calculate(month);
        }
        closing.close(user.accountId());
    }

    public void reopen(LoginUser user, YearMonth month) {
        accessService.require(user, PermissionCode.SUMMARY_MANAGE);
        closingRepository.findByTargetMonth(month.atDay(1))
                .orElseThrow(() -> new BusinessException("마감되지 않은 월입니다."))
                .reopen();
    }

}
