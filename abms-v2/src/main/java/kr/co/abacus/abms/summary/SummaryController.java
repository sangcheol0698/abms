package kr.co.abacus.abms.summary;

import java.time.YearMonth;
import java.util.List;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.security.LoginUser;

@Controller
@RequestMapping("/summary")
public class SummaryController {

    private final ProfitQueryService queryService;
    private final MonthClosingService closingService;

    public SummaryController(ProfitQueryService queryService, MonthClosingService closingService) {
        this.queryService = queryService;
        this.closingService = closingService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal LoginUser user, @RequestParam(required = false) @Nullable String month,
                        @RequestParam(required = false) @Nullable String basis, Model model) {
        if (!user.has(PermissionCode.DASHBOARD_READ)) {
            throw new AccessDeniedException("손익 조회 권한이 없습니다.");
        }
        YearMonth target = parse(month);
        model.addAttribute("report", queryService.monthReport(user, target, RevenueBasis.parse(basis)));
        model.addAttribute("canManage", user.has(PermissionCode.SUMMARY_MANAGE));
        model.addAttribute("isPast", target.isBefore(YearMonth.now()));
        return "summary/index";
    }

    @PostMapping("/recalculate")
    public String recalculate(@AuthenticationPrincipal LoginUser user, @RequestParam String month,
                         @RequestParam(required = false) @Nullable String basis, RedirectAttributes redirect) {
        YearMonth target = parse(month);
        CalculationResult result = closingService.recalculate(user, target);
        redirect.addFlashAttribute("calculation", result);
        if (result.skipped()) {
            Toast.error(redirect, result.warnings().getFirst());
        } else {
            Toast.success(redirect, target + " 손익을 재집계했습니다. (프로젝트 " + result.projectCount() + "건)");
        }
        return back(target, basis);
    }

    @PostMapping("/recalculate-year")
    public String recalculateYear(@AuthenticationPrincipal LoginUser user, @RequestParam int year, @RequestParam String month,
                                  @RequestParam(required = false) @Nullable String basis, RedirectAttributes redirect) {
        YearCalculationResult result = closingService.recalculateYear(user, year);
        redirect.addFlashAttribute("warnings", result.warnings());
        List<YearMonth> done = result.recalculated();
        String range = done.isEmpty() ? "" : done.getFirst().getMonthValue() + "~" + done.getLast().getMonthValue() + "월 ";
        Toast.success(redirect, year + "년 " + range + "손익을 다시 집계했습니다. (" + done.size() + "개월"
                + (result.skipped().isEmpty() ? "" : ", 마감 " + result.skipped().size() + "개월 건너뜀") + ")");
        return back(parse(month), basis);
    }

    @PostMapping("/close")
    public String close(@AuthenticationPrincipal LoginUser user, @RequestParam String month,
                         @RequestParam(required = false) @Nullable String basis, RedirectAttributes redirect) {
        YearMonth target = parse(month);
        closingService.close(user, target);
        Toast.success(redirect, target + " 손익을 마감했습니다. 마감된 월은 재집계되지 않습니다.");
        return back(target, basis);
    }

    @PostMapping("/reopen")
    public String reopen(@AuthenticationPrincipal LoginUser user, @RequestParam String month,
                         @RequestParam(required = false) @Nullable String basis, RedirectAttributes redirect) {
        YearMonth target = parse(month);
        closingService.reopen(user, target);
        Toast.success(redirect, target + " 마감을 해제했습니다.");
        return back(target, basis);
    }

    /** 작업 후에도 보던 매출 기준(청구/진행)을 유지한다. */
    private static String back(YearMonth month, @Nullable String basis) {
        RevenueBasis revenueBasis = RevenueBasis.parse(basis);
        return "redirect:/summary?month=" + month + (revenueBasis == RevenueBasis.MANAGED ? "&basis=" + revenueBasis.param() : "");
    }

    private static YearMonth parse(@Nullable String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException("월 형식이 올바르지 않습니다: " + month);
        }
    }

}
