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
import kr.co.abacus.abms.project.ProjectRevenuePlan.RevenuePlanInfo;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로젝트 매출 계획 (모달 폼 + 섹션 부분 갱신).
 */
@Controller
@RequestMapping("/projects/{projectId}/revenues")
public class ProjectRevenueController {

    private final ProjectService projectService;
    private final ProjectRevenueService revenueService;
    private final ProjectSections sections;

    public ProjectRevenueController(ProjectService projectService, ProjectRevenueService revenueService, ProjectSections sections) {
        this.projectService = projectService;
        this.revenueService = revenueService;
        this.sections = sections;
    }

    @GetMapping("/new")
    public String createModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, Model model) {
        Project project = projectService.getForWrite(user, projectId);
        RevenueForm form = new RevenueForm(revenueService.nextSequence(projectId), project.getPeriod().startDate(),
                RevenueType.DOWN_PAYMENT, null, null);
        return modal(model, project, null, form, FormErrors.none());
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, RevenueForm form,
                         Model model, HttpServletResponse response) {
        Project project = projectService.getForWrite(user, projectId);
        try {
            revenueService.add(user, projectId, form.toInfo());
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, project, null, form, FormErrors.global(e.getMessage()));
        }
        return sectionFromModal(user, projectId, model, response, form.sequence() + "차 매출 계획을 추가했습니다.");
    }

    @GetMapping("/{planId}/edit")
    public String editModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long planId, Model model) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectRevenuePlan plan = revenueService.get(projectId, planId);
        RevenueForm form = new RevenueForm(plan.getSequence(), plan.getRevenueDate(), plan.getType(), plan.getAmount().longValue(), plan.getMemo());
        return modal(model, project, plan, form, FormErrors.none());
    }

    @PostMapping("/{planId}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long planId,
                         RevenueForm form, Model model, HttpServletResponse response) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectRevenuePlan plan = revenueService.get(projectId, planId);
        try {
            revenueService.update(user, projectId, planId, form.toInfo());
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, project, plan, form, FormErrors.global(e.getMessage()));
        }
        return sectionFromModal(user, projectId, model, response, "매출 계획을 수정했습니다.");
    }

    @PostMapping("/{planId}/issue")
    public String issue(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long planId,
                        Model model, HttpServletResponse response) {
        revenueService.issue(user, projectId, planId);
        return section(user, projectId, model, response, "세금계산서 발행 처리했습니다.");
    }

    @PostMapping("/{planId}/cancel-issue")
    public String cancelIssue(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long planId,
                              Model model, HttpServletResponse response) {
        revenueService.cancelIssue(user, projectId, planId);
        return section(user, projectId, model, response, "발행을 취소했습니다.");
    }

    @PostMapping("/{planId}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long planId,
                         Model model, HttpServletResponse response) {
        revenueService.delete(user, projectId, planId);
        return section(user, projectId, model, response, "매출 계획을 삭제했습니다.");
    }

    private String modal(Model model, Project project, @Nullable ProjectRevenuePlan plan, RevenueForm form, FormErrors errors) {
        model.addAttribute("project", project);
        model.addAttribute("plan", plan);
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        return "project/revenueModal";
    }

    private String section(LoginUser user, Long projectId, Model model, HttpServletResponse response, String message) {
        Project project = projectService.get(projectId);
        model.addAttribute("section", sections.revenue(project, projectService.canWrite(user, project)));
        Htmx.toast(response, Htmx.ToastType.SUCCESS, message);
        return "project/revenueSection";
    }

    private String sectionFromModal(LoginUser user, Long projectId, Model model, HttpServletResponse response, String message) {
        response.setHeader("HX-Retarget", "#revenue-section");
        response.setHeader("HX-Reswap", "innerHTML");
        Htmx.event(response, "closeModal");
        return section(user, projectId, model, response, message);
    }

    public record RevenueForm(
            @Nullable Integer sequence,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate revenueDate,
            @Nullable RevenueType type,
            @Nullable Long amount,
            @Nullable String memo
    ) {

        RevenuePlanInfo toInfo() {
            if (sequence == null) {
                throw new BusinessException("차수를 입력하세요.");
            }
            if (revenueDate == null) {
                throw new BusinessException("청구일을 입력하세요.");
            }
            if (type == null) {
                throw new BusinessException("매출 유형을 선택하세요.");
            }
            if (amount == null || amount <= 0) {
                throw new BusinessException("금액은 0원보다 커야 합니다.");
            }
            return new RevenuePlanInfo(sequence, revenueDate, type, Money.wons(amount), memo);
        }

    }

}
