package kr.co.abacus.abms.project;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.PageView;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.common.web.Ui.SelectOption;
import kr.co.abacus.abms.department.DepartmentOptions;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.ProfitQueryService;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectRevenueService revenueService;
    private final ProjectAssignmentService assignmentService;
    private final PartyService partyService;
    private final DepartmentService departmentService;
    private final ProfitQueryService profitQueryService;
    private final ProjectSections sections;

    public ProjectController(ProjectService projectService, ProjectRevenueService revenueService,
                             ProjectAssignmentService assignmentService, PartyService partyService,
                             DepartmentService departmentService, ProfitQueryService profitQueryService,
                             ProjectSections sections) {
        this.projectService = projectService;
        this.revenueService = revenueService;
        this.assignmentService = assignmentService;
        this.partyService = partyService;
        this.departmentService = departmentService;
        this.profitQueryService = profitQueryService;
        this.sections = sections;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal LoginUser user, ProjectSearchForm search, @RequestParam(defaultValue = "0") int page,
                       HttpServletRequest request, Model model) {
        requireRead(user);
        Page<Project> result = projectService.search(user, search.toSearch(),
                PageRequest.of(Math.max(page, 0), 20, Sort.by(Sort.Direction.DESC, "period.startDate").and(Sort.by("id"))));
        String baseUrl = UriComponentsBuilder.fromPath("/projects").query(request.getQueryString()).build().toUriString();
        model.addAttribute("page", PageView.of(result, baseUrl));
        model.addAttribute("tree", departmentService.tree());
        model.addAttribute("partyNames", partyNames());
        model.addAttribute("search", search);
        if (Htmx.targets(request, "project-results")) {
            return "project/results";
        }
        model.addAttribute("departmentOptions", DepartmentOptions.of(departmentService.tree()));
        model.addAttribute("partyOptions", partyOptions());
        return "project/list";
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal LoginUser user, ProjectSearchForm search) {
        DepartmentTree tree = departmentService.tree();
        Map<Long, String> parties = partyNames();
        StringBuilder csv = new StringBuilder("﻿코드,프로젝트명,협력사,주관 부서,상태,계약금액,시작일,종료일\n");
        for (Project p : projectService.exportable(user, search.toSearch())) {
            csv.append(String.join(",", csv(p.getCode()), csv(p.getName()), csv(parties.getOrDefault(p.getPartyId(), "")),
                    csv(tree.nameOf(p.getLeadDepartmentId())), p.getStatus().label(), p.getContractAmount().amount().toPlainString(),
                    p.getPeriod().startDate().toString(), p.getPeriod().endDate() == null ? "" : p.getPeriod().endDate().toString())).append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"projects-" + LocalDate.now() + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal LoginUser user, Model model) {
        requireWrite(user);
        return form(model, ProjectForm.empty(projectService.suggestCode()), FormErrors.none(), null);
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @Valid @ModelAttribute("form") ProjectForm form, BindingResult binding,
                         Model model, HttpServletResponse response, RedirectAttributes redirect) {
        requireWrite(user);
        if (form.code() == null || form.code().isBlank()) {
            binding.rejectValue("code", "required", "프로젝트 코드를 입력하세요.");
        }
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), null);
        }
        try {
            Project project = projectService.create(user, form.code(), form.toInfo());
            Toast.success(redirect, project.getName() + " 프로젝트를 등록했습니다.");
            return "redirect:/projects/" + project.id();
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), null);
        }
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        Project project = projectService.getForRead(user, id);
        boolean canWrite = projectService.canWrite(user, project);
        model.addAttribute("project", project);
        model.addAttribute("party", partyService.get(project.getPartyId()));
        model.addAttribute("tree", departmentService.tree());
        model.addAttribute("canWrite", canWrite);
        model.addAttribute("revenue", sections.revenue(project, canWrite));
        model.addAttribute("staffing", sections.staffing(project, canWrite));
        // 손익 이력은 대시보드(손익) 조회 범위가 이 프로젝트를 포함할 때만 보여준다.
        boolean showHistory = profitQueryService.scope(user).coversProject(project.id(), project.getLeadDepartmentId());
        model.addAttribute("showHistory", showHistory);
        model.addAttribute("history", showHistory ? profitQueryService.projectHistory(id) : List.of());
        return "project/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        Project project = projectService.getForWrite(user, id);
        return form(model, ProjectForm.of(project), FormErrors.none(), project);
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        Project project = projectService.getForWrite(user, id);
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), project);
        }
        try {
            projectService.update(user, id, form.toInfo());
            Toast.success(redirect, "프로젝트 정보를 수정했습니다.");
            return "redirect:/projects/" + id;
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), project);
        }
    }

    @PostMapping("/{id}/complete")
    public String complete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        projectService.complete(user, id);
        Toast.success(redirect, "프로젝트를 완료 처리했습니다.");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        projectService.cancel(user, id);
        Toast.success(redirect, "프로젝트를 취소 처리했습니다.");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        Project project = projectService.getForWrite(user, id);
        projectService.delete(user, id);
        Toast.success(redirect, project.getName() + " 프로젝트를 삭제했습니다. 손익 집계에는 다음 재집계 시 반영됩니다.");
        return "redirect:/projects";
    }

    private String form(Model model, ProjectForm form, FormErrors errors, @Nullable Project project) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("project", project);
        model.addAttribute("departmentOptions", DepartmentOptions.of(departmentService.tree()));
        model.addAttribute("partyOptions", partyOptions());
        return "project/form";
    }

    private List<SelectOption> partyOptions() {
        return partyService.all().stream().map(p -> new SelectOption(p.id().toString(), p.getName())).toList();
    }

    private Map<Long, String> partyNames() {
        return partyService.all().stream().collect(Collectors.toMap(Party::id, Party::getName));
    }

    private static void requireRead(LoginUser user) {
        if (!user.has(PermissionCode.PROJECT_READ)) {
            throw new AccessDeniedException("프로젝트 조회 권한이 없습니다.");
        }
    }

    private static void requireWrite(LoginUser user) {
        if (!user.has(PermissionCode.PROJECT_WRITE)) {
            throw new AccessDeniedException("프로젝트 관리 권한이 없습니다.");
        }
    }

    private static String csv(String value) {
        String safe = !value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
        return safe.contains(",") || safe.contains("\"") ? "\"" + safe.replace("\"", "\"\"") + "\"" : safe;
    }

}
