package kr.co.abacus.abms.account;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import kr.co.abacus.abms.access.PermissionGroup;
import kr.co.abacus.abms.access.PermissionGroupService;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 계정 관리 (account.manage). URL 수준 권한은 SecurityConfig 에서 검사한다.
 */
@Controller
@RequestMapping("/admin/accounts")
public class AccountAdminController {

    private final AccountService accountService;
    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final PermissionGroupService groupService;

    public AccountAdminController(AccountService accountService, EmployeeRepository employeeRepository,
                                  DepartmentService departmentService, PermissionGroupService groupService) {
        this.accountService = accountService;
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
        this.groupService = groupService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) @Nullable Long issue, Model model) {
        List<Account> accounts = accountService.all();
        Map<Long, Employee> employees = employeeRepository.findAllById(accounts.stream().map(Account::getEmployeeId).toList())
                .stream().collect(Collectors.toMap(Employee::id, Function.identity()));
        Map<Long, String> groupNames = groupService.groups().stream().collect(Collectors.toMap(PermissionGroup::id, PermissionGroup::getName));
        Map<Long, List<String>> accountGroups = accounts.stream().collect(Collectors.toMap(Account::id,
                a -> groupService.groupIdsOf(a.id()).stream().map(id -> groupNames.getOrDefault(id, "?")).toList()));
        model.addAttribute("accounts", accounts);
        model.addAttribute("employees", employees);
        model.addAttribute("accountGroups", accountGroups);
        model.addAttribute("tree", departmentService.tree());
        model.addAttribute("issueEmployeeId", issue);
        return "admin/accounts";
    }

    @GetMapping("/issue")
    public String issueModal(@RequestParam(required = false) @Nullable Long employeeId, Model model) {
        model.addAttribute("candidates", accountService.employeesWithoutAccount());
        model.addAttribute("tree", departmentService.tree());
        model.addAttribute("selected", employeeId);
        model.addAttribute("errors", FormErrors.none());
        return "admin/issueModal";
    }

    @PostMapping("/issue")
    public String issue(@RequestParam(required = false) @Nullable Long employeeId, Model model, HttpServletResponse response) {
        try {
            if (employeeId == null) {
                throw new BusinessException("계정을 발급할 직원을 선택하세요.");
            }
            AccountService.IssuedAccount issued = accountService.issue(employeeId);
            model.addAttribute("username", issued.account().getUsername());
            model.addAttribute("password", issued.temporaryPassword());
            model.addAttribute("title", "계정을 발급했습니다");
            response.setHeader("HX-Trigger", "{\"accountsChanged\":true}");
            return "admin/passwordModal";
        } catch (BusinessException e) {
            response.setStatus(422);
            model.addAttribute("candidates", accountService.employeesWithoutAccount());
            model.addAttribute("tree", departmentService.tree());
            model.addAttribute("selected", employeeId);
            model.addAttribute("errors", FormErrors.global(e.getMessage()));
            return "admin/issueModal";
        }
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, Model model) {
        String password = accountService.resetPassword(id);
        model.addAttribute("username", accountService.get(id).getUsername());
        model.addAttribute("password", password);
        model.addAttribute("title", "비밀번호를 초기화했습니다");
        return "admin/passwordModal";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Long id, RedirectAttributes redirect) {
        accountService.enable(id);
        Toast.success(redirect, "계정을 활성화했습니다.");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/disable")
    public String disable(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        accountService.disable(id, user.accountId());
        Toast.success(redirect, "계정을 비활성화했습니다.");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id, RedirectAttributes redirect) {
        accountService.unlock(id);
        Toast.success(redirect, "계정 잠금을 해제했습니다.");
        return "redirect:/admin/accounts";
    }

}
