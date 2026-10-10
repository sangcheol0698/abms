package kr.co.abacus.abms.web.me;

import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.abacus.abms.access.PermissionRepository;
import kr.co.abacus.abms.admin.AccountService;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentService;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 내 정보: 프로필 요약, 보유 권한, 참여 프로젝트, 비밀번호 변경.
 */
@Controller
@RequestMapping("/me")
public class MeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final AccountService accountService;
    private final ProjectAssignmentService assignmentService;
    private final ProjectService projectService;
    private final PermissionRepository permissionRepository;

    public MeController(EmployeeService employeeService, DepartmentService departmentService, AccountService accountService,
                        ProjectAssignmentService assignmentService, ProjectService projectService,
                        PermissionRepository permissionRepository) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.accountService = accountService;
        this.assignmentService = assignmentService;
        this.projectService = projectService;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public String me(@AuthenticationPrincipal LoginUser user, Model model) {
        List<ProjectAssignment> assignments = assignmentService.assignmentsOfEmployee(user.employeeId()).stream()
                .filter(a -> a.getPeriod().endDate() == null || !a.getPeriod().endDate().isBefore(LocalDate.now().minusMonths(6)))
                .toList();
        model.addAttribute("employee", employeeService.get(user.employeeId()));
        model.addAttribute("account", accountService.get(user.accountId()));
        model.addAttribute("tree", departmentService.tree());
        model.addAttribute("assignments", assignments);
        List<kr.co.abacus.abms.project.Project> projects = assignments.stream().map(ProjectAssignment::getProjectId).distinct()
                .map(projectService::get).toList();
        model.addAttribute("projectNames", projects.stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.id(), p -> p.getName())));
        model.addAttribute("readableProjectIds", projects.stream()
                .filter(p -> projectService.canRead(user, p)).map(p -> p.id()).collect(java.util.stream.Collectors.toSet()));
        model.addAttribute("permissions", permissionRepository.findAllByOrderByCodeAsc());
        model.addAttribute("errors", FormErrors.none());
        return "me/index";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal LoginUser user, @RequestParam String currentPassword,
                                 @RequestParam String newPassword, @RequestParam String confirmPassword,
                                 Model model, HttpServletResponse response) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new BusinessException("새 비밀번호와 확인 값이 일치하지 않습니다.");
            }
            accountService.changePassword(user.accountId(), currentPassword, newPassword);
            Htmx.toast(response, Htmx.ToastType.SUCCESS, "비밀번호를 변경했습니다.");
            model.addAttribute("errors", FormErrors.none());
            model.addAttribute("done", true);
        } catch (BusinessException e) {
            response.setStatus(422);
            model.addAttribute("errors", FormErrors.global(e.getMessage()));
            model.addAttribute("done", false);
        }
        return "me/passwordForm";
    }

}
