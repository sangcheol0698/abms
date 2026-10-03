package kr.co.abacus.abms.palette;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeSearch;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectSearch;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;

/**
 * Cmd+K 명령 팔레트. 화면 이동·생성 명령과 직원/프로젝트/부서/협력사 검색 결과를 HTML 조각으로 돌려준다.
 * 검색은 각 목록 화면과 같은 서비스를 거치므로 권한 범위를 벗어나지 않는다.
 */
@Controller
public class CommandPaletteController {

    private static final int LIMIT = 5;

    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final DepartmentService departmentService;
    private final PartyService partyService;

    public CommandPaletteController(EmployeeService employeeService, ProjectService projectService,
                                    DepartmentService departmentService, PartyService partyService) {
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.departmentService = departmentService;
        this.partyService = partyService;
    }

    @GetMapping("/palette")
    public String search(@AuthenticationPrincipal LoginUser user, @RequestParam(required = false) @Nullable String q, Model model) {
        String keyword = q == null ? "" : q.trim();
        model.addAttribute("q", keyword);
        model.addAttribute("commands", commands(user).stream().filter(c -> c.matches(keyword)).toList());
        if (keyword.isEmpty()) {
            model.addAttribute("employees", List.of());
            model.addAttribute("projects", List.of());
            model.addAttribute("departments", List.of());
            model.addAttribute("parties", List.of());
            return "palette/results";
        }
        model.addAttribute("employees", employeeService.search(user, new EmployeeSearch(keyword, null, null, null, null, false),
                PageRequest.of(0, LIMIT, Sort.by("name"))).getContent());
        model.addAttribute("projects", user.has(PermissionCode.PROJECT_READ)
                ? projectService.search(user, new ProjectSearch(keyword, null, null, null, null), PageRequest.of(0, LIMIT)).getContent()
                : List.<Project>of());
        DepartmentTree tree = departmentService.tree();
        model.addAttribute("tree", tree);
        model.addAttribute("departments", tree.flatten().stream()
                .map(DepartmentTree.Node::department)
                .filter(d -> contains(d.getName(), keyword) || contains(d.getCode(), keyword))
                .limit(LIMIT)
                .toList());
        model.addAttribute("parties", user.has(PermissionCode.PARTY_READ)
                ? partyService.search(keyword, PageRequest.of(0, LIMIT, Sort.by("name"))).getContent()
                : List.<Party>of());
        return "palette/results";
    }

    private static List<Command> commands(LoginUser user) {
        List<Command> commands = new ArrayList<>();
        if (user.has(PermissionCode.DASHBOARD_READ)) {
            commands.add(Command.go("대시보드", "/", "home", "G D", "dashboard home"));
        }
        commands.add(Command.go("AI 어시스턴트", "/assistant", "sparkles", "G A", "assistant ai chat 챗봇"));
        commands.add(Command.go("직원", "/employees", "users", "G E", "employee 인원 사람"));
        commands.add(Command.go("부서", "/departments", "building", "G O", "department 조직도 팀"));
        if (user.has(PermissionCode.PROJECT_READ)) {
            commands.add(Command.go("프로젝트", "/projects", "folder", "G P", "project"));
        }
        if (user.has(PermissionCode.PARTY_READ)) {
            commands.add(Command.go("협력사", "/parties", "briefcase", "G C", "party 고객사 파트너 company"));
        }
        if (user.has(PermissionCode.DASHBOARD_READ)) {
            commands.add(Command.go("손익 현황", "/summary", "chart", "G S", "summary profit 매출 비용 마감"));
        }
        if (user.has(PermissionCode.REPORT_READ)) {
            commands.add(Command.go("주간 보고서", "/reports", "document", "G R", "report weekly"));
        }
        if (user.has(PermissionCode.ACCOUNT_MANAGE)) {
            commands.add(Command.go("계정 관리", "/admin/accounts", "key", null, "account 로그인"));
        }
        if (user.has(PermissionCode.PERMISSION_GROUP_MANAGE)) {
            commands.add(Command.go("권한 그룹", "/admin/permission-groups", "shield", null, "permission role"));
        }
        if (user.has(PermissionCode.SUMMARY_MANAGE)) {
            commands.add(Command.go("원가 정책", "/admin/cost-policies", "calculator", null, "cost policy 제경비 판관비"));
        }
        commands.add(Command.go("내 정보", "/me", "user", "G M", "me profile 비밀번호 password"));
        if (user.has(PermissionCode.EMPLOYEE_WRITE)) {
            commands.add(Command.create("직원 등록", "/employees/new", "employee new 추가 생성"));
        }
        if (user.has(PermissionCode.PROJECT_WRITE)) {
            commands.add(Command.create("프로젝트 등록", "/projects/new", "project new 추가 생성"));
        }
        if (user.has(PermissionCode.PARTY_WRITE)) {
            commands.add(Command.create("협력사 등록", "/parties/new", "party new 추가 생성"));
        }
        commands.add(Command.action("라이트 테마로 전환", "theme:light", "sun", "theme light 밝게"));
        commands.add(Command.action("다크 테마로 전환", "theme:dark", "moon", "theme dark 어둡게"));
        commands.add(Command.action("시스템 테마 따르기", "theme:system", "monitor", "theme system 자동"));
        commands.add(Command.action("단축키 보기", "shortcuts", "command", "shortcut keyboard 키보드 help 도움말"));
        return commands;
    }

    private static boolean contains(@Nullable String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * 팔레트 명령. href 가 있으면 이동, action 이 있으면 클라이언트 동작(테마 전환 등)을 실행한다.
     */
    public record Command(String group, String label, @Nullable String href, @Nullable String action, String icon,
                          @Nullable String shortcut, String keywords) {

        static Command go(String label, String href, String icon, @Nullable String shortcut, String keywords) {
            return new Command("이동", label, href, null, icon, shortcut, keywords);
        }

        static Command create(String label, String href, String keywords) {
            return new Command("만들기", label, href, null, "plus", null, keywords);
        }

        static Command action(String label, String action, String icon, String keywords) {
            return new Command("설정", label, null, action, icon, null, keywords);
        }

        boolean matches(String keyword) {
            return keyword.isEmpty() || contains(label, keyword) || contains(keywords, keyword);
        }

    }

}
