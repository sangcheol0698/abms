package kr.co.abacus.abms.project;

import java.time.LocalDate;

import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.project.ProjectExpense.ExpenseInfo;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로젝트 직접비 (모달 폼 + 섹션 부분 갱신).
 */
@Controller
@RequestMapping("/projects/{projectId}/expenses")
public class ProjectExpenseController {

    private final ProjectService projectService;
    private final ProjectExpenseService expenseService;
    private final ProjectSections sections;

    public ProjectExpenseController(ProjectService projectService, ProjectExpenseService expenseService, ProjectSections sections) {
        this.projectService = projectService;
        this.expenseService = expenseService;
        this.sections = sections;
    }

    @GetMapping("/new")
    public String createModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, Model model) {
        Project project = projectService.getForWrite(user, projectId);
        ExpenseForm form = new ExpenseForm(LocalDate.now(), ExpenseCategory.OUTSOURCING, null, null, null);
        return modal(model, project, null, form, FormErrors.none());
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, ExpenseForm form,
                         Model model, HttpServletResponse response) {
        Project project = projectService.getForWrite(user, projectId);
        try {
            expenseService.add(user, projectId, form.toInfo());
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, project, null, form, FormErrors.global(e.getMessage()));
        }
        return sectionFromModal(user, projectId, model, response, "직접비를 추가했습니다.");
    }

    @GetMapping("/{expenseId}/edit")
    public String editModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long expenseId, Model model) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectExpense expense = expenseService.get(projectId, expenseId);
        ExpenseForm form = new ExpenseForm(expense.getExpenseDate(), expense.getCategory(), expense.getAmount().longValue(),
                expense.getDescription(), expense.getMemo());
        return modal(model, project, expense, form, FormErrors.none());
    }

    @PostMapping("/{expenseId}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long expenseId,
                         ExpenseForm form, Model model, HttpServletResponse response) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectExpense expense = expenseService.get(projectId, expenseId);
        try {
            expenseService.update(user, projectId, expenseId, form.toInfo());
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, project, expense, form, FormErrors.global(e.getMessage()));
        }
        return sectionFromModal(user, projectId, model, response, "직접비를 수정했습니다.");
    }

    @PostMapping("/{expenseId}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long expenseId,
                         Model model, HttpServletResponse response) {
        expenseService.delete(user, projectId, expenseId);
        return section(user, projectId, model, response, "직접비를 삭제했습니다.");
    }

    private String modal(Model model, Project project, @Nullable ProjectExpense expense, ExpenseForm form, FormErrors errors) {
        model.addAttribute("project", project);
        model.addAttribute("expense", expense);
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        return "project/expenseModal";
    }

    private String section(LoginUser user, Long projectId, Model model, HttpServletResponse response, String message) {
        Project project = projectService.get(projectId);
        model.addAttribute("section", sections.expense(project, projectService.canWrite(user, project)));
        Htmx.toast(response, Htmx.ToastType.SUCCESS, message);
        return "project/expenseSection";
    }

    private String sectionFromModal(LoginUser user, Long projectId, Model model, HttpServletResponse response, String message) {
        response.setHeader("HX-Retarget", "#expense-section");
        response.setHeader("HX-Reswap", "innerHTML");
        Htmx.event(response, "closeModal");
        return section(user, projectId, model, response, message);
    }

    public record ExpenseForm(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate expenseDate,
            @Nullable ExpenseCategory category,
            @Nullable Long amount,
            @Nullable String description,
            @Nullable String memo
    ) {

        ExpenseInfo toInfo() {
            if (expenseDate == null) {
                throw new BusinessException("귀속일을 입력하세요.");
            }
            if (category == null) {
                throw new BusinessException("분류를 선택하세요.");
            }
            if (amount == null || amount <= 0) {
                throw new BusinessException("금액은 0원보다 커야 합니다.");
            }
            if (description == null || description.isBlank()) {
                throw new BusinessException("내용을 입력하세요.");
            }
            return new ExpenseInfo(expenseDate, category, Money.wons(amount), description, memo);
        }

    }

}
