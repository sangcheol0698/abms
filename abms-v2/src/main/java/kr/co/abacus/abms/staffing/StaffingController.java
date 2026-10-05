package kr.co.abacus.abms.staffing;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.employee.EmployeeJob;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.site.SiteService;
import kr.co.abacus.abms.staffing.AvailabilityService.Criteria;

/**
 * 가용 인력 찾기. 인력을 배치하는 사람(프로젝트 편집 권한)만 쓴다.
 */
@Controller
@RequestMapping("/staffing")
public class StaffingController {

    private final AvailabilityService availabilityService;
    private final SiteService siteService;

    public StaffingController(AvailabilityService availabilityService, SiteService siteService) {
        this.availabilityService = availabilityService;
        this.siteService = siteService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal LoginUser user,
                        @RequestParam(required = false) @Nullable String from, @RequestParam(required = false) @Nullable String to,
                        @RequestParam(required = false) @Nullable EmployeeJob job, @RequestParam(required = false) @Nullable String skills,
                        @RequestParam(required = false) @Nullable Integer minCareer, @RequestParam(required = false) @Nullable Long siteId,
                        @RequestParam(defaultValue = "false") boolean onlyAvailable, Model model) {
        if (!user.has(PermissionCode.PROJECT_WRITE)) {
            throw new AccessDeniedException("인력 찾기는 프로젝트 편집 권한이 필요합니다.");
        }
        YearMonth start = parse(from, YearMonth.now());
        YearMonth end = parse(to, start.plusMonths(2));
        Criteria criteria = new Criteria(start, end, job, skills, minCareer, siteId, onlyAvailable);
        model.addAttribute("criteria", criteria);
        model.addAttribute("sites", siteService.all());
        try {
            model.addAttribute("candidates", availabilityService.search(criteria, LocalDate.now()));
            model.addAttribute("error", null);
        } catch (BusinessException e) {
            model.addAttribute("candidates", java.util.List.of());
            model.addAttribute("error", e.getMessage());
        }
        return "staffing/index";
    }

    private static YearMonth parse(@Nullable String value, YearMonth fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

}
