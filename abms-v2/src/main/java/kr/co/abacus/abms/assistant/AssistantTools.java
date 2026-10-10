package kr.co.abacus.abms.assistant;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeSearch;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentService;
import kr.co.abacus.abms.project.ProjectRevenueService;
import kr.co.abacus.abms.project.ProjectSearch;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.MonthReport;
import kr.co.abacus.abms.summary.ProfitQueryService;

/**
 * 어시스턴트가 호출하는 조회 도구. 요청 사용자 기준으로 생성되어 사용자의 권한 범위 안에서만 데이터를 돌려준다.
 */
public class AssistantTools {

    private static final int MAX_RESULTS = 20;

    private final LoginUser user;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ProjectService projectService;
    private final ProjectRevenueService revenueService;
    private final ProjectAssignmentService assignmentService;
    private final PartyService partyService;
    private final ProfitQueryService profitQueryService;

    AssistantTools(LoginUser user, EmployeeService employeeService, DepartmentService departmentService,
                   ProjectService projectService, ProjectRevenueService revenueService,
                   ProjectAssignmentService assignmentService, PartyService partyService,
                   ProfitQueryService profitQueryService) {
        this.user = user;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.projectService = projectService;
        this.revenueService = revenueService;
        this.assignmentService = assignmentService;
        this.partyService = partyService;
        this.profitQueryService = profitQueryService;
    }

    @Tool(description = "직원을 이름 또는 이메일로 검색한다. 이름, 부서, 직급, 재직 상태, 상세 링크를 돌려준다.")
    public List<EmployeeItem> searchEmployees(@ToolParam(description = "이름 또는 이메일 일부", required = false) @Nullable String keyword) {
        DepartmentTree tree = departmentService.tree();
        EmployeeSearch search = new EmployeeSearch(keyword, null, null, null, null, false);
        return employeeService.search(user, search, PageRequest.of(0, MAX_RESULTS, Sort.by("name"))).stream()
                .map(e -> EmployeeItem.of(e, tree.nameOf(e.getDepartmentId())))
                .toList();
    }

    @Tool(description = "직원 ID로 상세 정보(입사일, 고용유형, 등급, 현재 투입 프로젝트)를 조회한다. 권한이 없으면 오류 메시지를 돌려준다.")
    public Object getEmployeeDetail(@ToolParam(description = "직원 ID") Long employeeId) {
        Employee employee = employeeService.get(employeeId);
        if (!employeeService.canRead(user, employee)) {
            return "이 직원의 상세 정보를 조회할 권한이 없습니다.";
        }
        DepartmentTree tree = departmentService.tree();
        List<String> projects = assignmentService.assignmentsOfEmployee(employeeId).stream()
                .filter(a -> a.isActiveOn(LocalDate.now()))
                .map(a -> projectService.get(a.getProjectId()).getName())
                .toList();
        return new EmployeeDetail(EmployeeItem.of(employee, tree.nameOf(employee.getDepartmentId())),
                employee.getJoinDate().toString(), employee.getType().label(), employee.getGrade().label(), projects);
    }

    @Tool(description = "부서를 이름으로 검색하고 부서 경로, 부서장, 소속 인원을 돌려준다. 이름을 비우면 전체 조직도를 돌려준다.")
    public List<DepartmentItem> searchDepartments(@ToolParam(description = "부서명 일부", required = false) @Nullable String name) {
        DepartmentTree tree = departmentService.tree();
        return tree.flatten().stream()
                .map(DepartmentTree.Node::department)
                .filter(d -> name == null || name.isBlank() || d.getName().contains(name.trim()))
                .limit(40)
                .map(d -> toDepartmentItem(d, tree))
                .toList();
    }

    @Tool(description = "프로젝트를 이름/코드와 상태로 검색한다. 상태 값: SCHEDULED(예약), IN_PROGRESS(진행 중), COMPLETED(완료), ON_HOLD(보류), CANCELLED(취소)")
    public Object searchProjects(@ToolParam(description = "프로젝트명 또는 코드 일부", required = false) @Nullable String keyword,
                                 @ToolParam(description = "프로젝트 상태", required = false) @Nullable String status) {
        if (!user.has(PermissionCode.PROJECT_READ)) {
            return "프로젝트 조회 권한이 없습니다.";
        }
        ProjectStatus projectStatus = parseStatus(status);
        DepartmentTree tree = departmentService.tree();
        ProjectSearch search = new ProjectSearch(keyword, projectStatus, null, null, null);
        return projectService.search(user, search, PageRequest.of(0, MAX_RESULTS, Sort.by(Sort.Direction.DESC, "period.startDate"))).stream()
                .map(p -> ProjectItem.of(p, partyService.get(p.getPartyId()).getName(), tree.nameOf(p.getLeadDepartmentId())))
                .toList();
    }

    @Tool(description = "프로젝트 ID로 상세 정보(계약금액, 기간, 매출 계획, 투입 인력)를 조회한다.")
    public Object getProjectDetail(@ToolParam(description = "프로젝트 ID") Long projectId) {
        Project project = projectService.get(projectId);
        if (!projectService.canRead(user, project)) {
            return "이 프로젝트를 조회할 권한이 없습니다.";
        }
        DepartmentTree tree = departmentService.tree();
        List<String> plans = revenueService.plans(projectId).stream()
                .map(r -> r.getSequence() + "차 " + r.getType().label() + " " + r.getRevenueDate() + " "
                        + r.getAmount().formatted() + "원 " + (r.isIssued() ? "(발행)" : "(미발행)"))
                .toList();
        List<String> members = assignmentService.assignments(projectId).stream()
                .map(a -> describe(a))
                .toList();
        return new ProjectDetail(ProjectItem.of(project, partyService.get(project.getPartyId()).getName(), tree.nameOf(project.getLeadDepartmentId())),
                project.getDescription(), plans, members);
    }

    @Tool(description = "협력사(고객사)를 이름으로 검색한다.")
    public Object searchParties(@ToolParam(description = "협력사명 일부", required = false) @Nullable String name) {
        if (!user.has(PermissionCode.PARTY_READ)) {
            return "협력사 조회 권한이 없습니다.";
        }
        List<kr.co.abacus.abms.party.Party> parties = partyService.search(name, PageRequest.of(0, MAX_RESULTS, Sort.by("name"))).getContent();
        List<Long> ids = parties.stream().map(kr.co.abacus.abms.party.Party::id).toList();
        java.util.Map<Long, kr.co.abacus.abms.party.PartyContact> contacts = partyService.primaryContacts(ids);
        java.util.Map<Long, Long> counts = projectService.partyProjectCounts(ids);
        return parties.stream()
                .map(p -> {
                    kr.co.abacus.abms.party.PartyContact c = contacts.get(p.id());
                    return new PartyItem(p.id(), p.getName(), c == null ? null : c.getName() + " (" + c.getRole().label() + ")",
                            counts.getOrDefault(p.id(), 0L), "/parties/" + p.id());
                })
                .toList();
    }

    @Tool(description = "월별 손익(매출, 비용, 이익)을 프로젝트별·부서별로 조회한다. 월은 yyyy-MM 형식이다.")
    public Object getMonthlyProfit(@ToolParam(description = "조회 월 (예: 2026-02)") String month) {
        if (!user.has(PermissionCode.DASHBOARD_READ)) {
            return "손익 조회 권한이 없습니다.";
        }
        YearMonth ym;
        try {
            ym = YearMonth.parse(month.trim());
        } catch (DateTimeParseException e) {
            return "월 형식이 올바르지 않습니다. 예: 2026-02";
        }
        MonthReport report = profitQueryService.monthReport(user, ym);
        if (!report.calculated()) {
            return ym + " 손익이 아직 집계되지 않았습니다.";
        }
        return new ProfitSummary(ym.toString(), report.revenue().formatted(), report.cost().formatted(),
                report.profit().formatted(), report.margin() + "%",
                report.projects().stream().map(r -> r.name() + ": 매출 " + r.revenue().formatted() + ", 비용 "
                        + r.cost().formatted() + ", 이익 " + r.profit().formatted()).toList(),
                report.departments().stream().map(r -> r.name() + ": 이익 " + r.profit().formatted()).toList(),
                "/summary?month=" + ym);
    }

    private String describe(ProjectAssignment a) {
        String name;
        try {
            name = employeeService.get(a.getEmployeeId()).getName();
        } catch (RuntimeException e) {
            name = "(삭제된 직원)";
        }
        return name + " " + (a.getRole() == null ? "" : a.getRole().label()) + " " + a.getPeriod().startDate()
                + " ~ " + (a.getPeriod().endDate() == null ? "" : a.getPeriod().endDate());
    }

    private DepartmentItem toDepartmentItem(Department d, DepartmentTree tree) {
        String path = String.join(" > ", tree.path(d.id()).stream().map(Department::getName).toList());
        String leader = null;
        if (d.getLeaderEmployeeId() != null) {
            try {
                leader = employeeService.get(d.getLeaderEmployeeId()).getName();
            } catch (RuntimeException ignored) {
                leader = null;
            }
        }
        return new DepartmentItem(d.id(), d.getName(), d.getType().label(), path, leader,
                employeeService.members(d.id()).size(), "/departments/" + d.id());
    }

    private static @Nullable ProjectStatus parseStatus(@Nullable String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim();
        for (ProjectStatus s : ProjectStatus.values()) {
            if (s.name().equalsIgnoreCase(normalized) || s.label().equals(normalized)) {
                return s;
            }
        }
        return null;
    }

    public record EmployeeItem(Long id, String name, String department, String position, String status, String email, String link) {
        static EmployeeItem of(Employee e, String departmentName) {
            return new EmployeeItem(e.id(), e.getName(), departmentName, e.getPosition().label(), e.getStatus().label(),
                    e.getEmail(), "/employees/" + e.id());
        }
    }

    public record EmployeeDetail(EmployeeItem employee, String joinDate, String type, String grade, List<String> currentProjects) {
    }

    public record DepartmentItem(Long id, String name, String type, String path, @Nullable String leader, int memberCount, String link) {
    }

    public record ProjectItem(Long id, String code, String name, String status, String party, String leadDepartment,
                              String contractAmount, String startDate, @Nullable String endDate, String link) {
        static ProjectItem of(Project p, String partyName, String departmentName) {
            return new ProjectItem(p.id(), p.getCode(), p.getName(), p.getStatus().label(), partyName, departmentName,
                    p.getContractAmount().formatted() + "원", p.getPeriod().startDate().toString(),
                    p.getPeriod().endDate() == null ? null : p.getPeriod().endDate().toString(), "/projects/" + p.id());
        }
    }

    public record ProjectDetail(ProjectItem project, @Nullable String description, List<String> revenuePlans, List<String> members) {
    }

    public record PartyItem(Long id, String name, @Nullable String primaryContact, long projectCount, String link) {
    }

    public record ProfitSummary(String month, String revenue, String cost, String profit, String margin,
                                List<String> projects, List<String> departments, String link) {
    }

}
