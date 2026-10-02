package kr.co.abacus.abms.report;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.common.web.PageView;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 주간 보고서 (report.read). URL 수준 권한은 SecurityConfig 에서 검사한다.
 */
@Controller
@RequestMapping("/reports")
public class WeeklyReportController {

    private final WeeklyReportService reportService;
    private final AccountRepository accountRepository;

    public WeeklyReportController(WeeklyReportService reportService, AccountRepository accountRepository) {
        this.reportService = reportService;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var result = reportService.list(PageRequest.of(Math.max(page, 0), 20));
        model.addAttribute("page", PageView.of(result, UriComponentsBuilder.fromPath("/reports").toUriString()));
        model.addAttribute("authors", accountRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.id(), a -> a.getUsername())));
        model.addAttribute("defaultDate", WeeklyReportService.mondayOf(LocalDate.now()));
        return "report/list";
    }

    @PostMapping
    public String generate(@AuthenticationPrincipal LoginUser user,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate date,
                           RedirectAttributes redirect) {
        WeeklyReport report = reportService.generate(user, date == null ? LocalDate.now() : date);
        Toast.success(redirect, report.getGenerator() == WeeklyReport.Generator.AI
                ? "AI 주간 보고서를 생성했습니다. 내용을 검토해 주세요." : "주간 보고서를 생성했습니다. (템플릿 기반)");
        return "redirect:/reports/" + report.id();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        WeeklyReport report = reportService.get(id);
        model.addAttribute("report", report);
        model.addAttribute("author", accountRepository.findById(report.getAuthorAccountId()).map(a -> a.getUsername()).orElse("-"));
        return "report/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("report", reportService.get(id));
        return "report/edit";
    }

    @PostMapping("/{id}")
    public String edit(@PathVariable Long id, @RequestParam String title, @RequestParam String content, RedirectAttributes redirect) {
        reportService.edit(id, title, content);
        Toast.success(redirect, "보고서를 저장했습니다.");
        return "redirect:/reports/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        reportService.delete(user, id);
        Toast.success(redirect, "보고서를 삭제했습니다.");
        return "redirect:/reports";
    }

}
