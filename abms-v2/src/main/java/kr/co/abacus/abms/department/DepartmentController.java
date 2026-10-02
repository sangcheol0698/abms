package kr.co.abacus.abms.department;

import java.time.Year;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.security.DataScope;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.ProfitQueryService;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final ProjectRepository projectRepository;
    private final ProfitQueryService profitQueryService;

    public DepartmentController(DepartmentService departmentService, EmployeeService employeeService,
                                ProjectRepository projectRepository, ProfitQueryService profitQueryService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.projectRepository = projectRepository;
        this.profitQueryService = profitQueryService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal LoginUser user, @RequestParam(required = false) @Nullable Long selected, Model model) {
        DepartmentTree tree = departmentService.tree();
        Long selectedId = selected != null && tree.get(selected) != null ? selected
                : tree.get(user.departmentId()) != null ? user.departmentId()
                : tree.roots().isEmpty() ? null : tree.roots().getFirst().id();
        model.addAttribute("tree", tree);
        model.addAttribute("selectedId", selectedId);
        if (selectedId != null) {
            addDetail(user, selectedId, tree, model);
        }
        return "department/index";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, HttpServletRequest request, Model model) {
        if (!Htmx.targets(request, "department-detail")) {
            return "redirect:/departments?selected=" + id;
        }
        addDetail(user, id, departmentService.tree(), model);
        return "department/detail";
    }

    @GetMapping("/new")
    public String createModal(@RequestParam(required = false) @Nullable Long parentId, Model model) {
        return formModal(model, null, new DepartmentForm(null, null, DepartmentType.TEAM, parentId), FormErrors.none());
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, DepartmentForm form, Model model,
                         HttpServletRequest request, HttpServletResponse response) {
        try {
            form.validate();
            Department department = departmentService.create(user, form.code(), form.name(), form.type(), form.parentId());
            return Htmx.redirect(request, response, "/departments?selected=" + department.id(),
                    new Toast("success", department.getName() + " 부서를 만들었습니다."));
        } catch (BusinessException e) {
            response.setStatus(422);
            return formModal(model, null, form, FormErrors.global(e.getMessage()));
        }
    }

    @GetMapping("/{id}/edit")
    public String editModal(@PathVariable Long id, Model model) {
        Department d = departmentService.get(id);
        return formModal(model, d, new DepartmentForm(d.getCode(), d.getName(), d.getType(), d.getParentId()), FormErrors.none());
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, DepartmentForm form, Model model,
                         HttpServletRequest request, HttpServletResponse response) {
        Department department = departmentService.get(id);
        try {
            form.validateName();
            departmentService.update(user, id, form.name(), form.type(), form.parentId());
            return Htmx.redirect(request, response, "/departments?selected=" + id, new Toast("success", "부서 정보를 수정했습니다."));
        } catch (BusinessException e) {
            response.setStatus(422);
            return formModal(model, department, form, FormErrors.global(e.getMessage()));
        }
    }

    @GetMapping("/{id}/leader")
    public String leaderModal(@PathVariable Long id, Model model) {
        Department department = departmentService.get(id);
        model.addAttribute("department", department);
        model.addAttribute("employees", employeeService.activeEmployees());
        model.addAttribute("tree", departmentService.tree());
        return "department/leaderModal";
    }

    @PostMapping("/{id}/leader")
    public String assignLeader(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                               @RequestParam(required = false) @Nullable Long leaderEmployeeId,
                               HttpServletRequest request, HttpServletResponse response) {
        departmentService.assignLeader(user, id, leaderEmployeeId);
        return Htmx.redirect(request, response, "/departments?selected=" + id, new Toast("success", "부서장을 지정했습니다."));
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        Department department = departmentService.get(id);
        departmentService.delete(user, id);
        Toast.success(redirect, department.getName() + " 부서를 삭제했습니다.");
        return "redirect:/departments" + (department.getParentId() == null ? "" : "?selected=" + department.getParentId());
    }

    private void addDetail(LoginUser user, Long id, DepartmentTree tree, Model model) {
        Department department = departmentService.get(id);
        Set<Long> subtree = tree.subtreeIds(id);
        List<Employee> members = departmentService.members(id);
        model.addAttribute("department", department);
        model.addAttribute("tree", tree);
        model.addAttribute("members", members);
        model.addAttribute("subtreeMemberCount", subtree.stream().mapToInt(d -> departmentService.members(d).size()).sum());
        model.addAttribute("leader", department.getLeaderEmployeeId() == null ? null
                : employeeService.findAll(Set.of(department.getLeaderEmployeeId())).stream().findFirst().orElse(null));
        model.addAttribute("projects", user.has(PermissionCode.PROJECT_READ)
                ? projectRepository.findAllByLeadDepartmentIdInOrderByPeriodStartDateDesc(subtree) : List.of());
        DataScope scope = profitQueryService.scope(user);
        boolean showProfit = scope.all() || scope.departmentIds().containsAll(subtree);
        model.addAttribute("trend", showProfit ? profitQueryService.departmentTrend(subtree, Year.now().getValue()) : null);
        model.addAttribute("canWrite", user.has(PermissionCode.DEPARTMENT_WRITE));
    }

    private String formModal(Model model, @Nullable Department department, DepartmentForm form, FormErrors errors) {
        model.addAttribute("department", department);
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("departmentOptions", DepartmentOptions.of(departmentService.tree()));
        return "department/formModal";
    }

    public record DepartmentForm(@Nullable String code, @Nullable String name, @Nullable DepartmentType type, @Nullable Long parentId) {

        void validate() {
            if (code == null || code.isBlank()) {
                throw new BusinessException("부서 코드를 입력하세요.");
            }
            validateName();
        }

        void validateName() {
            if (name == null || name.isBlank()) {
                throw new BusinessException("부서명을 입력하세요.");
            }
            if (type == null) {
                throw new BusinessException("부서 유형을 선택하세요.");
            }
        }

    }

}
