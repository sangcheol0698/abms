package kr.co.abacus.abms.access;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import kr.co.abacus.abms.account.Account;
import kr.co.abacus.abms.account.AccountService;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 권한 그룹 관리 (permission.group.manage). URL 수준 권한은 SecurityConfig 에서 검사한다.
 */
@Controller
@RequestMapping("/admin/permission-groups")
public class PermissionGroupController {

    private final kr.co.abacus.abms.common.audit.AuditQueryService auditQueryService;

    private final PermissionGroupService groupService;
    private final AccountService accountService;
    private final EmployeeRepository employeeRepository;

    public PermissionGroupController(PermissionGroupService groupService, AccountService accountService,
                                     EmployeeRepository employeeRepository,
            kr.co.abacus.abms.common.audit.AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
        this.groupService = groupService;
        this.accountService = accountService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<PermissionGroup> groups = groupService.groups();
        model.addAttribute("groups", groups);
        model.addAttribute("memberCounts", groups.stream().collect(Collectors.toMap(PermissionGroup::id, g -> groupService.memberCount(g.id()))));
        model.addAttribute("grantCounts", groups.stream().collect(Collectors.toMap(PermissionGroup::id, g -> groupService.grants(g.id()).size())));
        return "admin/permissionGroups";
    }

    @GetMapping("/new")
    public String createModal(Model model) {
        model.addAttribute("errors", FormErrors.none());
        return "admin/groupCreateModal";
    }

    @PostMapping
    public String create(@RequestParam(required = false) @Nullable String name, @RequestParam(required = false) @Nullable String description,
                         Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (name == null || name.isBlank()) {
                throw new BusinessException("그룹명을 입력하세요.");
            }
            PermissionGroup group = groupService.create(name, description == null ? "" : description);
            return Htmx.redirect(request, response, "/admin/permission-groups/" + group.id(),
                    new Toast("success", "권한 그룹을 만들었습니다. 부여할 권한을 선택하세요."));
        } catch (BusinessException e) {
            response.setStatus(422);
            model.addAttribute("errors", FormErrors.global(e.getMessage()));
            return "admin/groupCreateModal";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PermissionGroup group = groupService.get(id);
        List<Account> accounts = accountService.all();
        Map<Long, Employee> employees = employeeRepository.findAllById(accounts.stream().map(Account::getEmployeeId).toList())
                .stream().collect(Collectors.toMap(Employee::id, Function.identity()));
        Set<Long> memberIds = Set.copyOf(groupService.memberAccountIds(id));
        model.addAttribute("group", group);
        model.addAttribute("permissions", groupService.permissions());
        model.addAttribute("grants", groupService.grants(id));
        model.addAttribute("members", accounts.stream().filter(a -> memberIds.contains(a.id())).toList());
        model.addAttribute("candidates", accounts.stream().filter(a -> !memberIds.contains(a.id())).toList());
        model.addAttribute("employees", employees);
        model.addAttribute("auditHistory", auditQueryService.history("PermissionGroup", id, 30));
        return "admin/permissionGroup";
    }

    /**
     * @param grant "권한ID:범위" 형식의 체크박스 값 목록
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) @Nullable String description,
                         @RequestParam(required = false) @Nullable List<String> grant, RedirectAttributes redirect) {
        Map<Long, Set<PermissionScope>> grants = new HashMap<>();
        for (String value : grant == null ? List.<String>of() : grant) {
            String[] parts = value.split(":");
            if (parts.length != 2) {
                continue;
            }
            grants.computeIfAbsent(Long.valueOf(parts[0]), k -> EnumSet.noneOf(PermissionScope.class)).add(PermissionScope.valueOf(parts[1]));
        }
        groupService.update(id, name, description == null ? "" : description, grants);
        Toast.success(redirect, "권한 그룹을 저장했습니다. 변경 내용은 사용자가 다시 로그인하면 반영됩니다.");
        return "redirect:/admin/permission-groups/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        groupService.delete(id);
        Toast.success(redirect, "권한 그룹을 삭제했습니다.");
        return "redirect:/admin/permission-groups";
    }

    @PostMapping("/{id}/members")
    public String addMember(@PathVariable Long id, @RequestParam(required = false) @Nullable Long accountId, RedirectAttributes redirect) {
        if (accountId == null) {
            throw new BusinessException("추가할 계정을 선택하세요.");
        }
        groupService.addMember(id, accountId);
        Toast.success(redirect, "계정을 그룹에 추가했습니다.");
        return "redirect:/admin/permission-groups/" + id;
    }

    @PostMapping("/{id}/members/{accountId}/delete")
    public String removeMember(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @PathVariable Long accountId,
                               RedirectAttributes redirect) {
        groupService.removeMember(id, accountId, user.accountId());
        Toast.success(redirect, "계정을 그룹에서 제외했습니다.");
        return "redirect:/admin/permission-groups/" + id;
    }

}
