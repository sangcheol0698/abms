package kr.co.abacus.abms.dashboard;

import java.time.Year;
import java.util.stream.IntStream;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.security.LoginUser;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal LoginUser user, @RequestParam(required = false) Integer year, Model model) {
        if (!user.has(PermissionCode.DASHBOARD_READ)) {
            return "redirect:/me";
        }
        int currentYear = Year.now().getValue();
        int targetYear = year == null ? currentYear : year;
        model.addAttribute("dashboard", dashboardService.load(user, targetYear));
        model.addAttribute("years", IntStream.rangeClosed(currentYear - 3, currentYear + 1).boxed().sorted((a, b) -> b - a).toList());
        return "dashboard/index";
    }

}
