package kr.co.abacus.abms.application.chat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
import kr.co.abacus.abms.application.dashboard.inbound.DashboardFinder;
import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.application.party.inbound.PartyFinder;
import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.project.ProjectQueryService;
import kr.co.abacus.abms.application.project.dto.ProjectDetail;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentFinder;
import kr.co.abacus.abms.application.positionhistory.inbound.PositionHistoryFinder;
import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryFinder;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessQueryTools {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final int RESOLVE_CANDIDATE_LIMIT = 20;

    private final EmployeeFinder employeeFinder;
    private final DepartmentRepository departmentRepository;
    private final DepartmentFinder departmentFinder;
    private final ProjectQueryService projectQueryService;
    private final ProjectAssignmentFinder projectAssignmentFinder;
    private final PositionHistoryFinder positionHistoryFinder;
    private final DashboardFinder dashboardFinder;
    private final PartyFinder partyFinder;
    private final PartyQueryService partyQueryService;
    private final MonthlyRevenueSummaryFinder monthlyRevenueSummaryFinder;

    @Setter
    private @Nullable Consumer<String> toolCallNotifier;

    @Tool(description = "직원 목록을 검색하는 도구입니다. 이름으로 검색하고 최대 20명까지 반환합니다.")
    public EmployeeSearchResult searchEmployees(@Nullable String name, @Nullable Integer limit) {
        notifyToolCall("searchEmployees");

        int pageSize = normalizeLimit(limit);
        EmployeeSearchCondition condition = new EmployeeSearchCondition(name, null, null, null, null, null);
        Page<EmployeeSummary> page = employeeFinder.search(condition, PageRequest.of(0, pageSize));

        List<EmployeeSearchItem> employees = page.getContent().stream()
                .map(summary -> new EmployeeSearchItem(
                        summary.employeeId(),
                        summary.name(),
                        summary.departmentName(),
                        summary.position().getDescription(),
                        summary.grade().getDescription(),
                        summary.status().getDescription(),
                        summary.email().address(),
                        "/employees/" + summary.employeeId(),
                        summary.departmentId() != null ? "/departments/" + summary.departmentId() : null))
                .toList();

        return new EmployeeSearchResult(
                name,
                (int) page.getTotalElements(),
                employees.size(),
                employees);
    }

    @Tool(description = "프로젝트 목록을 검색하는 도구입니다. 이름과 상태(예: IN_PROGRESS, 진행 중)로 필터링하고 최대 20건을 반환합니다.")
    public ProjectSearchResult searchProjects(
            @Nullable String name,
            @Nullable String status,
            @Nullable Integer limit) {
        notifyToolCall("searchProjects");

        int pageSize = normalizeLimit(limit);
        ProjectStatus projectStatus = toProjectStatus(status);
        ProjectSearchCondition condition = new ProjectSearchCondition(
                name,
                projectStatus != null ? List.of(projectStatus) : null,
                null,
                null,
                null);
        Page<ProjectSummary> page = projectQueryService.search(condition, PageRequest.of(0, pageSize));

        List<ProjectSearchItem> projects = page.getContent().stream()
                .map(summary -> {
                    String partyName = partyQueryService.getPartyName(summary.partyId());
                    return new ProjectSearchItem(
                            summary.projectId(),
                            summary.code(),
                            summary.name(),
                            partyName,
                            summary.status().name(),
                            summary.status().getDescription(),
                            summary.contractAmount().amount().longValue(),
                            summary.startDate().toString(),
                            summary.endDate() != null ? summary.endDate().toString() : null,
                            "/projects/" + summary.projectId(),
                            summary.partyId() != null ? "/parties/" + summary.partyId() : null);
                })
                .toList();

        return new ProjectSearchResult(
                name,
                status,
                (int) page.getTotalElements(),
                projects.size(),
                projects);
    }

    @Tool(description = "프로젝트 ID로 상세 정보를 조회하는 도구입니다. 협력사, 주관 부서, 기간, 상태, 계약금과 상세 페이지 링크를 반환합니다.")
    public ProjectDetailInfo getProjectDetail(Long projectId) {
        notifyToolCall("getProjectDetail");

        ProjectDetail detail = projectQueryService.findDetail(projectId);
        return new ProjectDetailInfo(
                detail.projectId(),
                detail.code(),
                detail.name(),
                detail.status().name(),
                detail.status().getDescription(),
                detail.contractAmount(),
                detail.startDate().toString(),
                detail.endDate() != null ? detail.endDate().toString() : null,
                detail.partyId(),
                detail.partyName(),
                detail.leadDepartmentId(),
                detail.leadDepartmentName(),
                "/projects/" + detail.projectId(),
                "/parties/" + detail.partyId(),
                detail.leadDepartmentId() != null ? "/departments/" + detail.leadDepartmentId() : null);
    }

    @Tool(description = "대시보드 요약 지표를 조회하는 도구입니다. 총 직원 수, 진행 중 프로젝트 수, 당월 신규 입사자 수, 휴직자 수를 반환합니다.")
    public DashboardSummaryInfo getDashboardSummary() {
        notifyToolCall("getDashboardSummary");

        DashboardSummaryResponse summary = dashboardFinder.getDashboardSummary();
        return new DashboardSummaryInfo(
                summary.totalEmployeesCount(),
                summary.activeProjectsCount(),
                summary.newEmployeesCount(),
                summary.onLeaveEmployeesCount());
    }

    @Tool(description = "협력사 목록을 검색하는 도구입니다. 이름으로 검색하고 최대 20건을 반환합니다.")
    public PartySearchResult searchParties(@Nullable String name, @Nullable Integer limit) {
        notifyToolCall("searchParties");

        int pageSize = normalizeLimit(limit);
        Page<Party> page = partyFinder.getParties(PageRequest.of(0, pageSize), name);
        List<PartySearchItem> parties = page.getContent().stream()
                .map(party -> new PartySearchItem(
                        party.getIdOrThrow(),
                        party.getName(),
                        party.getCeoName(),
                        party.getSalesRepName(),
                        party.getSalesRepPhone(),
                        party.getSalesRepEmail(),
                        "/parties/" + party.getIdOrThrow()))
                .toList();

        return new PartySearchResult(name, (int) page.getTotalElements(), parties.size(), parties);
    }

    @Tool(description = "특정 협력사의 프로젝트 목록을 조회하는 도구입니다. 협력사명을 받아 프로젝트 목록과 링크를 반환합니다.")
    public @Nullable PartyProjectsResult getPartyProjects(String partyName) {
        notifyToolCall("getPartyProjects");

        @Nullable Party resolvedParty = resolvePartyByName(partyName);
        if (resolvedParty == null) {
            return null;
        }

        List<Project> projects = projectQueryService.findAllByPartyId(resolvedParty.getIdOrThrow());
        List<PartyProjectItem> items = projects.stream()
                .map(project -> new PartyProjectItem(
                        project.getIdOrThrow(),
                        project.getCode(),
                        project.getName(),
                        project.getStatus().name(),
                        project.getStatus().getDescription(),
                        project.getContractAmount().amount().longValue(),
                        project.getPeriod().startDate().toString(),
                        project.getPeriod().endDate() != null ? project.getPeriod().endDate().toString() : null,
                        "/projects/" + project.getIdOrThrow()))
                .toList();

        return new PartyProjectsResult(
                resolvedParty.getIdOrThrow(),
                resolvedParty.getName(),
                "/parties/" + resolvedParty.getIdOrThrow(),
                items.size(),
                items);
    }

    @Tool(description = "월별 매출 집계를 조회하는 도구입니다. yearMonth(yyyyMM, 예: 202602)를 입력하면 매출/비용/이익을 반환합니다.")
    public @Nullable MonthlyRevenueSummaryInfo getMonthlyRevenueSummary(@Nullable String yearMonth) {
        notifyToolCall("getMonthlyRevenueSummary");

        String targetYearMonth = normalizeYearMonth(yearMonth);
        try {
            MonthlyRevenueSummary summary = monthlyRevenueSummaryFinder.findByTargetMonth(targetYearMonth);
            return new MonthlyRevenueSummaryInfo(
                    targetYearMonth,
                    summary.getSummaryDate().toString(),
                    summary.getRevenueAmount().amount().longValue(),
                    summary.getCostAmount().amount().longValue(),
                    summary.getProfitAmount().amount().longValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Tool(description = "부서 소속 직원을 조회하는 도구입니다. 부서명과 직원 이름 필터로 검색하며 최대 20명까지 반환합니다.")
    public @Nullable DepartmentEmployeesResult getDepartmentEmployees(
            String departmentName,
            @Nullable String employeeName,
            @Nullable Integer limit) {
        notifyToolCall("getDepartmentEmployees");

        int pageSize = normalizeLimit(limit);
        @Nullable Department department = departmentRepository.findByName(departmentName).orElse(null);
        if (department == null) {
            return null;
        }

        DepartmentDetail departmentDetail = departmentFinder.findDetail(department.getIdOrThrow());
        Page<EmployeeSummary> page = departmentFinder.getEmployees(
                department.getIdOrThrow(),
                employeeName,
                PageRequest.of(0, pageSize));

        List<DepartmentEmployeeItem> employees = page.getContent().stream()
                .map(summary -> new DepartmentEmployeeItem(
                        summary.employeeId(),
                        summary.name(),
                        summary.position().getDescription(),
                        summary.grade().getDescription(),
                        summary.status().getDescription(),
                        summary.email().address(),
                        "/employees/" + summary.employeeId()))
                .toList();

        return new DepartmentEmployeesResult(
                departmentDetail.id(),
                departmentDetail.name(),
                employeeName,
                (int) page.getTotalElements(),
                employees.size(),
                "/departments/" + departmentDetail.id(),
                employees);
    }

    @Tool(description = "프로젝트 투입 인력 목록을 조회하는 도구입니다. 프로젝트명을 입력하면 역할/기간/직원 링크를 반환합니다.")
    public @Nullable ProjectAssignmentsResult getProjectAssignments(String projectName) {
        notifyToolCall("getProjectAssignments");

        @Nullable ProjectSummary resolvedProject = resolveProjectByName(projectName);
        if (resolvedProject == null) {
            return null;
        }

        ProjectDetail projectDetail = projectQueryService.findDetail(resolvedProject.projectId());
        List<ProjectAssignment> assignments = projectAssignmentFinder.findByProjectId(resolvedProject.projectId());

        List<ProjectAssignmentItem> items = assignments.stream()
                .map(assignment -> {
                    String assignedEmployeeName = "직원 정보 없음";
                    String position = "정보 없음";
                    String status = "정보 없음";

                    @Nullable Employee assignedEmployee = resolveEmployeeById(assignment.getEmployeeId());
                    if (assignedEmployee != null) {
                        assignedEmployeeName = assignedEmployee.getName();
                        position = assignedEmployee.getPosition().getDescription();
                        status = assignedEmployee.getStatus().getDescription();
                    }

                    return new ProjectAssignmentItem(
                            assignment.getIdOrThrow(),
                            assignment.getEmployeeId(),
                            assignedEmployeeName,
                            position,
                            status,
                            assignment.getRole() != null ? assignment.getRole().name() : null,
                            assignment.getRole() != null ? assignment.getRole().getDescription() : null,
                            assignment.getPeriod().startDate().toString(),
                            assignment.getPeriod().endDate() != null ? assignment.getPeriod().endDate().toString() : null,
                            "/employees/" + assignment.getEmployeeId());
                })
                .toList();

        return new ProjectAssignmentsResult(
                projectDetail.projectId(),
                projectDetail.name(),
                "/projects/" + projectDetail.projectId(),
                items.size(),
                items);
    }

    @Tool(description = "직원의 직급 이력을 조회하는 도구입니다. 직원명을 입력하면 과거/현재 직급 이력과 기간을 반환합니다.")
    public @Nullable EmployeePositionHistoryResult getEmployeePositionHistory(String employeeName) {
        notifyToolCall("getEmployeePositionHistory");

        @Nullable Employee employee = resolveEmployeeByName(employeeName);
        if (employee == null) {
            return null;
        }

        List<PositionHistory> histories = positionHistoryFinder.findAll(employee.getIdOrThrow()).stream()
                .sorted((a, b) -> b.getPeriod().startDate().compareTo(a.getPeriod().startDate()))
                .toList();

        List<PositionHistoryItem> items = histories.stream()
                .map(history -> new PositionHistoryItem(
                        history.getIdOrThrow(),
                        history.getPosition().name(),
                        history.getPosition().getDescription(),
                        history.getPeriod().startDate().toString(),
                        history.getPeriod().endDate() != null ? history.getPeriod().endDate().toString() : null))
                .toList();

        return new EmployeePositionHistoryResult(
                employee.getIdOrThrow(),
                employee.getName(),
                employee.getPosition().name(),
                employee.getPosition().getDescription(),
                "/employees/" + employee.getIdOrThrow(),
                items.size(),
                items);
    }

    private void notifyToolCall(String toolName) {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept(toolName);
        }
    }

    private int normalizeLimit(@Nullable Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }

    private @Nullable ProjectStatus toProjectStatus(@Nullable String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return ProjectStatus.fromDescription(status);
    }

    private String normalizeYearMonth(@Nullable String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            return LocalDate.now().format(YEAR_MONTH_FORMATTER);
        }
        return yearMonth.trim();
    }

    private @Nullable ProjectSummary resolveProjectByName(String projectName) {
        ProjectSearchCondition condition = new ProjectSearchCondition(projectName, null, null, null, null);
        Page<ProjectSummary> page = projectQueryService.search(condition, PageRequest.of(0, RESOLVE_CANDIDATE_LIMIT));
        if (page.isEmpty()) {
            return null;
        }

        return page.getContent().stream()
                .filter(project -> project.name().equalsIgnoreCase(projectName))
                .findFirst()
                .orElse(page.getContent().getFirst());
    }

    private @Nullable Employee resolveEmployeeByName(String employeeName) {
        EmployeeSearchCondition condition = new EmployeeSearchCondition(employeeName, null, null, null, null, null);
        Page<EmployeeSummary> page = employeeFinder.search(condition, PageRequest.of(0, RESOLVE_CANDIDATE_LIMIT));
        if (page.isEmpty()) {
            return null;
        }

        @Nullable EmployeeSummary selected = page.getContent().stream()
                .filter(summary -> summary.name().equalsIgnoreCase(employeeName))
                .findFirst()
                .orElse(page.getContent().getFirst());
        return resolveEmployeeById(selected.employeeId());
    }

    private @Nullable Party resolvePartyByName(String partyName) {
        Page<Party> page = partyFinder.getParties(PageRequest.of(0, RESOLVE_CANDIDATE_LIMIT), partyName);
        if (page.isEmpty()) {
            return null;
        }

        return page.getContent().stream()
                .filter(party -> party.getName().equalsIgnoreCase(partyName))
                .findFirst()
                .orElse(page.getContent().getFirst());
    }

    private @Nullable Employee resolveEmployeeById(Long employeeId) {
        try {
            return employeeFinder.find(employeeId);
        } catch (EmployeeNotFoundException e) {
            return null;
        }
    }

    public record EmployeeSearchResult(
            @Nullable String keyword,
            int totalCount,
            int returnedCount,
            List<EmployeeSearchItem> employees) {

    }

    public record EmployeeSearchItem(
            Long id,
            String name,
            String departmentName,
            String position,
            String grade,
            String status,
            String email,
            String link,
            @Nullable String departmentLink) {

    }

    public record ProjectSearchResult(
            @Nullable String keyword,
            @Nullable String statusFilter,
            int totalCount,
            int returnedCount,
            List<ProjectSearchItem> projects) {

    }

    public record ProjectSearchItem(
            Long id,
            String code,
            String name,
            String partyName,
            String status,
            String statusDescription,
            long contractAmount,
            String startDate,
            @Nullable String endDate,
            String link,
            @Nullable String partyLink) {

    }

    public record ProjectDetailInfo(
            Long projectId,
            String code,
            String name,
            String status,
            String statusDescription,
            Long contractAmount,
            String startDate,
            @Nullable String endDate,
            Long partyId,
            String partyName,
            @Nullable Long leadDepartmentId,
            @Nullable String leadDepartmentName,
            String link,
            String partyLink,
            @Nullable String leadDepartmentLink) {

    }

    public record DashboardSummaryInfo(
            int totalEmployeesCount,
            int activeProjectsCount,
            int newEmployeesCount,
            int onLeaveEmployeesCount) {

    }

    public record PartySearchResult(
            @Nullable String keyword,
            int totalCount,
            int returnedCount,
            List<PartySearchItem> parties) {

    }

    public record PartySearchItem(
            Long id,
            String name,
            @Nullable String ceo,
            @Nullable String manager,
            @Nullable String contact,
            @Nullable String email,
            String link) {

    }

    public record PartyProjectsResult(
            Long partyId,
            String partyName,
            String partyLink,
            int projectCount,
            List<PartyProjectItem> projects) {

    }

    public record PartyProjectItem(
            Long id,
            String code,
            String name,
            String status,
            String statusDescription,
            long contractAmount,
            String startDate,
            @Nullable String endDate,
            String link) {

    }

    public record MonthlyRevenueSummaryInfo(
            String yearMonth,
            String summaryDate,
            long revenue,
            long cost,
            long profit) {

    }

    public record DepartmentEmployeesResult(
            Long departmentId,
            String departmentName,
            @Nullable String nameFilter,
            int totalCount,
            int returnedCount,
            String departmentLink,
            List<DepartmentEmployeeItem> employees) {

    }

    public record DepartmentEmployeeItem(
            Long id,
            String name,
            String position,
            String grade,
            String status,
            String email,
            String link) {

    }

    public record ProjectAssignmentsResult(
            Long projectId,
            String projectName,
            String projectLink,
            int assignmentCount,
            List<ProjectAssignmentItem> assignments) {

    }

    public record ProjectAssignmentItem(
            Long assignmentId,
            Long employeeId,
            String employeeName,
            String position,
            String employeeStatus,
            @Nullable String role,
            @Nullable String roleDescription,
            String startDate,
            @Nullable String endDate,
            String employeeLink) {

    }

    public record EmployeePositionHistoryResult(
            Long employeeId,
            String employeeName,
            String currentPosition,
            String currentPositionDescription,
            String employeeLink,
            int historyCount,
            List<PositionHistoryItem> histories) {

    }

    public record PositionHistoryItem(
            Long id,
            String position,
            String positionDescription,
            String startDate,
            @Nullable String endDate) {

    }

}
