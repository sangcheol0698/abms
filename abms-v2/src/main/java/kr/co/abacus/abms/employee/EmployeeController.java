package kr.co.abacus.abms.employee;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.common.audit.AuditQueryService;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.PageView;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.department.DepartmentOptions;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentService;
import kr.co.abacus.abms.project.ProjectPlaceService;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.security.LoginUser;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    private final ProjectPlaceService placeService;
    private final EmployeePhotoService photoService;

    private static final int PAGE_SIZE = 20;

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ProjectAssignmentService assignmentService;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final AuditQueryService auditQueryService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService,
                              ProjectAssignmentService assignmentService, ProjectRepository projectRepository,
                              AccountRepository accountRepository, AuditQueryService auditQueryService,
                              ProjectPlaceService placeService, EmployeePhotoService photoService) {
        this.photoService = photoService;
        this.auditQueryService = auditQueryService;
        this.placeService = placeService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.assignmentService = assignmentService;
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal LoginUser user, EmployeeSearchForm search,
                       @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model) {
        DepartmentTree tree = departmentService.tree();
        Page<Employee> result = employeeService.search(user, search.toSearch(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("name").and(Sort.by("id"))));
        String baseUrl = UriComponentsBuilder.fromPath("/employees").query(request.getQueryString()).build().toUriString();
        model.addAttribute("page", PageView.of(result, baseUrl));
        model.addAttribute("tree", tree);
        model.addAttribute("search", search);
        if (Htmx.targets(request, "employee-results")) {
            return "employee/results";
        }
        model.addAttribute("departmentOptions", DepartmentOptions.of(tree));
        return "employee/list";
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal LoginUser user, EmployeeSearchForm search) {
        DepartmentTree tree = departmentService.tree();
        StringBuilder csv = new StringBuilder("﻿이름,이메일,연락처,부서,직급,등급,고용유형,상태,직무,근무 형태,보유 기술,입사일,경력 시작일,퇴사일\n");
        for (Employee e : employeeService.exportable(user, search.toSearch())) {
            csv.append(String.join(",", csvCell(e.getName()), csvCell(e.getEmail()), csvCell(e.getPhone() == null ? "" : e.getPhone()),
                    csvCell(tree.nameOf(e.getDepartmentId())), e.getPosition().label(), e.getGrade().label(), e.getType().label(), e.getStatus().label(),
                    e.getJob() == null ? "" : csvCell(e.getJob().label()), e.getWorkType() == null ? "" : csvCell(e.getWorkType().label()),
                    csvCell(e.getSkills() == null ? "" : e.getSkills()), e.getJoinDate().toString(),
                    e.getCareerStartDate() == null ? "" : e.getCareerStartDate().toString(),
                    e.getResignationDate() == null ? "" : e.getResignationDate().toString())).append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees-" + LocalDate.now() + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal LoginUser user, Model model) {
        requireWrite(user);
        return form(model, EmployeeForm.empty(), FormErrors.none(), null);
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @Valid @ModelAttribute("form") EmployeeForm form,
                         BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), null);
        }
        try {
            Money salary = form.annualSalary() == null ? null : Money.wons(form.annualSalary());
            Employee employee = employeeService.create(user, form.toProfile(), salary);
            Toast.success(redirect, employee.getName() + " 직원을 등록했습니다.");
            return "redirect:/employees/" + employee.id();
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), null);
        }
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        Employee employee = employeeService.getForRead(user, id);
        DepartmentTree tree = departmentService.tree();
        List<ProjectAssignment> assignments = assignmentService.assignmentsOfEmployee(id);
        Map<Long, Project> projects = projectRepository.findAllById(assignments.stream().map(ProjectAssignment::getProjectId).toList())
                .stream().collect(Collectors.toMap(Project::id, Function.identity()));
        model.addAttribute("employee", employee);
        model.addAttribute("tree", tree);
        List<ProjectAssignment> visibleAssignments = assignments.stream().filter(a -> projects.containsKey(a.getProjectId())).toList();
        model.addAttribute("assignments", visibleAssignments);
        model.addAttribute("insight", EmployeeInsight.of(employee, visibleAssignments, java.time.LocalDate.now()));
        model.addAttribute("projects", projects);
        model.addAttribute("workplace", placeService.workplaceOf(employee.getDepartmentId(), visibleAssignments, projects, tree, java.time.LocalDate.now()));
        model.addAttribute("payrolls", employeeService.payrolls(id));
        model.addAttribute("positions", employeeService.positionHistories(id));
        model.addAttribute("canWrite", employeeService.canFullWrite(user, employee.getDepartmentId()));
        model.addAttribute("canEditOwn", employeeService.canWriteOwnProfile(user, employee));
        model.addAttribute("canChangePhoto", photoService.canChange(user, employee));
        model.addAttribute("account", accountRepository.findByEmployeeId(id).orElse(null));
        model.addAttribute("auditHistory", auditQueryService.history("Employee", id, 30));
        return "employee/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        Employee employee = employeeService.get(id);
        if (employeeService.canFullWrite(user, employee.getDepartmentId())) {
            return form(model, EmployeeForm.of(employee), FormErrors.none(), employee);
        }
        if (employeeService.canWriteOwnProfile(user, employee)) {
            model.addAttribute("employee", employee);
            model.addAttribute("form", OwnProfileForm.of(employee));
            model.addAttribute("errors", FormErrors.none());
            return "employee/ownProfile";
        }
        throw new AccessDeniedException("직원 정보를 수정할 권한이 없습니다.");
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                         @Valid @ModelAttribute("form") EmployeeForm form, BindingResult binding, Model model,
                         HttpServletResponse response, RedirectAttributes redirect) {
        Employee employee = employeeService.get(id);
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), employee);
        }
        try {
            employeeService.update(user, id, form.toProfile());
            Toast.success(redirect, "직원 정보를 수정했습니다.");
            return "redirect:/employees/" + id;
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), employee);
        }
    }

    @PostMapping("/{id}/profile")
    public String updateOwnProfile(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                                   @Valid @ModelAttribute("form") OwnProfileForm form, BindingResult binding, Model model,
                                   HttpServletResponse response, RedirectAttributes redirect) {
        Employee employee = employeeService.get(id);
        if (binding.hasErrors()) {
            response.setStatus(422);
            model.addAttribute("employee", employee);
            model.addAttribute("errors", FormErrors.of(binding));
            return "employee/ownProfile";
        }
        employeeService.updateOwnProfile(user, id, form.name(), form.birthDate(), form.phone(), form.skills());
        Toast.success(redirect, "내 정보를 수정했습니다.");
        return "redirect:/employees/" + id;
    }

    // ------------------------------------------------------------------
    // 상태 변경 (모달)
    // ------------------------------------------------------------------

    @GetMapping("/{id}/resign")
    public String resignModal(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.get(id));
        model.addAttribute("errors", FormErrors.none());
        return "employee/resignModal";
    }

    @PostMapping("/{id}/resign")
    public String resign(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate resignationDate,
                         Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (resignationDate == null) {
                throw new BusinessException("퇴사일을 입력하세요.");
            }
            employeeService.resign(user, id, resignationDate);
        } catch (BusinessException e) {
            response.setStatus(422);
            model.addAttribute("employee", employeeService.get(id));
            model.addAttribute("errors", FormErrors.global(e.getMessage()));
            return "employee/resignModal";
        }
        return redirectToDetail(request, response, id, "퇴사 처리했습니다.");
    }

    @PostMapping("/{id}/leave")
    public String takeLeave(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        employeeService.takeLeave(user, id);
        Toast.success(redirect, "휴직 처리했습니다.");
        return "redirect:/employees/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        employeeService.activate(user, id);
        Toast.success(redirect, "재직 상태로 변경했습니다.");
        return "redirect:/employees/" + id;
    }

    @GetMapping("/{id}/promote")
    public String promoteModal(@PathVariable Long id, Model model) {
        Employee employee = employeeService.get(id);
        model.addAttribute("employee", employee);
        model.addAttribute("errors", FormErrors.none());
        return "employee/promoteModal";
    }

    @PostMapping("/{id}/promote")
    public String promote(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                          @RequestParam EmployeePosition position, @RequestParam(required = false) @Nullable EmployeeGrade grade,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate effectiveDate,
                          Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            employeeService.promote(user, id, position, grade, effectiveDate == null ? LocalDate.now() : effectiveDate);
        } catch (BusinessException e) {
            response.setStatus(422);
            model.addAttribute("employee", employeeService.get(id));
            model.addAttribute("errors", FormErrors.global(e.getMessage()));
            return "employee/promoteModal";
        }
        return redirectToDetail(request, response, id, "직급/등급을 변경했습니다.");
    }

    @GetMapping("/{id}/salary")
    public String salaryModal(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.get(id));
        model.addAttribute("errors", FormErrors.none());
        return "employee/salaryModal";
    }

    @PostMapping("/{id}/salary")
    public String changeSalary(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                               @RequestParam(required = false) @Nullable Long annualSalary,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate startDate,
                               Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (annualSalary == null || annualSalary <= 0) {
                throw new BusinessException("연봉을 입력하세요.");
            }
            if (startDate == null) {
                throw new BusinessException("적용일을 입력하세요.");
            }
            employeeService.changeSalary(user, id, Money.wons(annualSalary), startDate);
        } catch (BusinessException e) {
            response.setStatus(422);
            model.addAttribute("employee", employeeService.get(id));
            model.addAttribute("errors", FormErrors.global(e.getMessage()));
            return "employee/salaryModal";
        }
        return redirectToDetail(request, response, id, "연봉을 등록했습니다.");
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        employeeService.delete(user, id);
        Toast.success(redirect, "직원을 삭제했습니다. 삭제된 직원 목록에서 복구할 수 있습니다.");
        return "redirect:/employees";
    }

    @PostMapping("/{id}/restore")
    public String restore(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        employeeService.restore(user, id);
        Toast.success(redirect, "직원을 복구했습니다.");
        return "redirect:/employees/" + id;
    }

    private String form(Model model, EmployeeForm form, FormErrors errors, @Nullable Employee employee) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("employee", employee);
        model.addAttribute("departmentOptions", DepartmentOptions.of(departmentService.tree()));
        return "employee/form";
    }

    private static String redirectToDetail(HttpServletRequest request, HttpServletResponse response, Long id, String message) {
        return Htmx.redirect(request, response, "/employees/" + id, new Toast("success", message));
    }

    private static void requireWrite(LoginUser user) {
        if (!user.has(PermissionCode.EMPLOYEE_WRITE)) {
            throw new AccessDeniedException("직원 등록 권한이 없습니다.");
        }
    }

    static String csvCell(String value) {
        String safe = value;
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe; // CSV 수식 주입 방지
        }
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

}
