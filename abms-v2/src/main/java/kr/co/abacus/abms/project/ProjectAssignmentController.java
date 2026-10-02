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
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로젝트 투입 인력 (모달 폼 + 섹션 부분 갱신).
 */
@Controller
@RequestMapping("/projects/{projectId}/assignments")
public class ProjectAssignmentController {

    private final ProjectService projectService;
    private final ProjectAssignmentService assignmentService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ProjectSections sections;

    public ProjectAssignmentController(ProjectService projectService, ProjectAssignmentService assignmentService,
                                       EmployeeService employeeService, DepartmentService departmentService,
                                       ProjectSections sections) {
        this.projectService = projectService;
        this.assignmentService = assignmentService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.sections = sections;
    }

    @GetMapping("/new")
    public String createModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, Model model) {
        Project project = projectService.getForWrite(user, projectId);
        LocalDate start = LocalDate.now().isBefore(project.getPeriod().startDate()) || LocalDate.now().isAfter(endOf(project))
                ? project.getPeriod().startDate() : LocalDate.now();
        return modal(model, project, null, new AssignmentForm(null, AssignmentRole.DEV, start, project.getPeriod().endDate()), FormErrors.none());
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, AssignmentForm form,
                         Model model, HttpServletResponse response) {
        Project project = projectService.getForWrite(user, projectId);
        try {
            form.validate();
            assignmentService.assign(user, projectId, form.employeeId(), form.role(), form.startDate(), form.endDate());
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, project, null, form, FormErrors.global(e.getMessage()));
        }
        return sectionFromModal(user, projectId, model, response, "투입 인력을 추가했습니다.");
    }

    @GetMapping("/{assignmentId}/edit")
    public String editModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long assignmentId, Model model) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectAssignment a = assignmentService.get(projectId, assignmentId);
        return modal(model, project, a, new AssignmentForm(a.getEmployeeId(), a.getRole(), a.getPeriod().startDate(), a.getPeriod().endDate()),
                FormErrors.none());
    }

    @PostMapping("/{assignmentId}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long assignmentId,
                         AssignmentForm form, Model model, HttpServletResponse response) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectAssignment assignment = assignmentService.get(projectId, assignmentId);
        try {
            form.validate();
            assignmentService.update(user, projectId, assignmentId, form.employeeId(), form.role(), form.startDate(), form.endDate());
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, project, assignment, form, FormErrors.global(e.getMessage()));
        }
        return sectionFromModal(user, projectId, model, response, "투입 정보를 수정했습니다.");
    }

    @PostMapping("/{assignmentId}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long projectId, @PathVariable Long assignmentId,
                         Model model, HttpServletResponse response) {
        assignmentService.delete(user, projectId, assignmentId);
        Project project = projectService.get(projectId);
        model.addAttribute("section", sections.staffing(project, projectService.canWrite(user, project)));
        Htmx.toast(response, Htmx.ToastType.SUCCESS, "투입 정보를 삭제했습니다.");
        return "project/staffingSection";
    }

    private String modal(Model model, Project project, @Nullable ProjectAssignment assignment, AssignmentForm form, FormErrors errors) {
        model.addAttribute("project", project);
        model.addAttribute("assignment", assignment);
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("employees", employeeService.activeEmployees());
        model.addAttribute("tree", departmentService.tree());
        return "project/assignmentModal";
    }

    private String sectionFromModal(LoginUser user, Long projectId, Model model, HttpServletResponse response, String message) {
        Project project = projectService.get(projectId);
        model.addAttribute("section", sections.staffing(project, projectService.canWrite(user, project)));
        response.setHeader("HX-Retarget", "#staffing-section");
        response.setHeader("HX-Reswap", "innerHTML");
        Htmx.event(response, "closeModal");
        Htmx.toast(response, Htmx.ToastType.SUCCESS, message);
        return "project/staffingSection";
    }

    private static LocalDate endOf(Project project) {
        return project.getPeriod().endDate() == null ? LocalDate.MAX : project.getPeriod().endDate();
    }

    public record AssignmentForm(
            @Nullable Long employeeId,
            @Nullable AssignmentRole role,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate endDate
    ) {

        void validate() {
            if (employeeId == null) {
                throw new BusinessException("투입할 직원을 선택하세요.");
            }
            if (startDate == null) {
                throw new BusinessException("투입 시작일을 입력하세요.");
            }
            if (endDate != null && endDate.isBefore(startDate)) {
                throw new BusinessException("투입 종료일은 시작일 이후여야 합니다.");
            }
        }

    }

}
