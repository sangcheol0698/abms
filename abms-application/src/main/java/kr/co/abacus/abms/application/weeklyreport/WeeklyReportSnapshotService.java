package kr.co.abacus.abms.application.weeklyreport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryFinder;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

@Service
@Transactional(readOnly = true)
class WeeklyReportSnapshotService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final MonthlyRevenueSummaryFinder monthlyRevenueSummaryFinder;

    WeeklyReportSnapshotService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            MonthlyRevenueSummaryFinder monthlyRevenueSummaryFinder
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.monthlyRevenueSummaryFinder = monthlyRevenueSummaryFinder;
    }

    WeeklyReportSnapshot createSnapshot(LocalDate weekStart, LocalDate weekEnd) {
        List<Employee> employees = employeeRepository.search(new EmployeeSearchCondition(null, null, null, null, null, null));
        List<Department> departments = departmentRepository.findAllByDeletedFalse();
        List<Project> projects = projectRepository.findAllByDeletedFalse();
        List<ProjectAssignment> assignments = projectAssignmentRepository.findActiveAssignments(weekStart, weekEnd);

        return new WeeklyReportSnapshot(
                weekStart,
                weekEnd,
                createEmployeeSection(weekStart, weekEnd, employees, departments),
                createProjectSection(weekStart, weekEnd, projects, assignments),
                createRevenueSection(weekEnd),
                createRiskSection(employees, departments, weekEnd, projects)
        );
    }

    private WeeklyReportSnapshot.EmployeeSection createEmployeeSection(
            LocalDate weekStart,
            LocalDate weekEnd,
            List<Employee> employees,
            List<Department> departments
    ) {
        Map<Long, Department> departmentById = departments.stream()
                .collect(Collectors.toMap(Department::getIdOrThrow, Function.identity()));

        List<WeeklyReportSnapshot.DepartmentOnLeave> onLeaveSignals = employees.stream()
                .filter(employee -> employee.getStatus() == EmployeeStatus.ON_LEAVE)
                .collect(Collectors.groupingBy(Employee::getDepartmentId, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Department department = departmentById.get(entry.getKey());
                    String departmentName = department != null ? department.getName() : "부서 미확인";
                    return new WeeklyReportSnapshot.DepartmentOnLeave(entry.getKey(), departmentName, entry.getValue());
                })
                .toList();

        long resignedThisWeek = employees.stream()
                .filter(employee -> {
                    LocalDate resignationDate = employee.getResignationDate();
                    return resignationDate != null
                            && !resignationDate.isBefore(weekStart)
                            && !resignationDate.isAfter(weekEnd);
                })
                .count();

        return new WeeklyReportSnapshot.EmployeeSection(
                employees.size(),
                employees.stream().filter(employee -> employee.getStatus() == EmployeeStatus.ACTIVE).count(),
                employees.stream().filter(employee -> employee.getStatus() == EmployeeStatus.ON_LEAVE).count(),
                employees.stream().filter(employee -> employee.getStatus() == EmployeeStatus.RESIGNED).count(),
                departments.size(),
                employees.stream()
                        .filter(employee -> !employee.getJoinDate().isBefore(weekStart) && !employee.getJoinDate().isAfter(weekEnd))
                        .count(),
                resignedThisWeek,
                true,
                onLeaveSignals
        );
    }

    private WeeklyReportSnapshot.ProjectSection createProjectSection(
            LocalDate weekStart,
            LocalDate weekEnd,
            List<Project> projects,
            List<ProjectAssignment> assignments
    ) {
        long startedContractAmount = projects.stream()
                .filter(project -> !project.getPeriod().startDate().isBefore(weekStart) && !project.getPeriod().startDate().isAfter(weekEnd))
                .mapToLong(project -> project.getContractAmount().amount().longValue())
                .sum();
        long endedContractAmount = projects.stream()
                .filter(project -> {
                    LocalDate endDate = project.getPeriod().endDate();
                    return endDate != null
                            && !endDate.isBefore(weekStart)
                            && !endDate.isAfter(weekEnd);
                })
                .mapToLong(project -> project.getContractAmount().amount().longValue())
                .sum();

        List<WeeklyReportSnapshot.ProjectDeadlineItem> endingSoonProjects = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS)
                .filter(project -> {
                    LocalDate endDate = project.getPeriod().endDate();
                    return endDate != null
                            && endDate.isAfter(weekEnd)
                            && !endDate.isAfter(weekEnd.plusDays(7));
                })
                .sorted(Comparator.comparing(project -> project.getPeriod().endDate()))
                .limit(5)
                .map(project -> {
                    LocalDate endDate = project.getPeriod().endDate();
                    if (endDate == null) {
                        throw new IllegalStateException("종료일이 없는 프로젝트는 종료 임박 목록에 포함될 수 없습니다.");
                    }
                    return new WeeklyReportSnapshot.ProjectDeadlineItem(
                            project.getIdOrThrow(),
                            project.getName(),
                            endDate);
                })
                .toList();

        List<WeeklyReportSnapshot.ProjectCoverageItem> projectsWithoutAssignments = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS)
                .filter(project -> projectAssignmentRepository.findOverlappingAssignments(
                        project.getIdOrThrow(),
                        weekEnd,
                        weekEnd
                ).isEmpty())
                .limit(5)
                .map(project -> new WeeklyReportSnapshot.ProjectCoverageItem(project.getIdOrThrow(), project.getName()))
                .toList();

        long assignmentChangeProjectCount = assignments.stream()
                .filter(assignment -> {
                    LocalDate startDate = assignment.getPeriod().startDate();
                    LocalDate endDate = assignment.getPeriod().endDate();
                    boolean startedThisWeek = !startDate.isBefore(weekStart) && !startDate.isAfter(weekEnd);
                    boolean endedThisWeek = endDate != null && !endDate.isBefore(weekStart) && !endDate.isAfter(weekEnd);
                    return startedThisWeek || endedThisWeek;
                })
                .map(ProjectAssignment::getProjectId)
                .distinct()
                .count();

        return new WeeklyReportSnapshot.ProjectSection(
                projects.stream().filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS).count(),
                projects.stream().filter(project -> project.getStatus() == ProjectStatus.COMPLETED).count(),
                projects.stream().filter(project -> project.getStatus() == ProjectStatus.CANCELLED).count(),
                projects.stream()
                        .filter(project -> !project.getPeriod().startDate().isBefore(weekStart) && !project.getPeriod().startDate().isAfter(weekEnd))
                        .count(),
                projects.stream()
                        .filter(project -> {
                            LocalDate endDate = project.getPeriod().endDate();
                            return endDate != null
                                    && !endDate.isBefore(weekStart)
                                    && !endDate.isAfter(weekEnd);
                        })
                        .count(),
                startedContractAmount,
                endedContractAmount,
                assignmentChangeProjectCount,
                endingSoonProjects,
                projectsWithoutAssignments
        );
    }

    private WeeklyReportSnapshot.RevenueSection createRevenueSection(LocalDate weekEnd) {
        String targetMonth = YEAR_MONTH_FORMATTER.format(weekEnd);
        return monthlyRevenueSummaryFinder.findOptionalByTargetMonth(targetMonth)
                .map(summary -> new WeeklyReportSnapshot.RevenueSection(
                        true,
                        targetMonth,
                        summary.getRevenueAmount().amount().longValue(),
                        summary.getCostAmount().amount().longValue(),
                        summary.getProfitAmount().amount().longValue()
                ))
                .orElseGet(() -> new WeeklyReportSnapshot.RevenueSection(false, targetMonth, null, null, null));
    }

    private WeeklyReportSnapshot.RiskSection createRiskSection(
            List<Employee> employees,
            List<Department> departments,
            LocalDate weekEnd,
            List<Project> projects
    ) {
        Map<Long, String> departmentNames = departments.stream()
                .collect(Collectors.toMap(Department::getIdOrThrow, Department::getName));

        List<String> onLeaveDepartmentNames = employees.stream()
                .filter(employee -> employee.getStatus() == EmployeeStatus.ON_LEAVE)
                .collect(Collectors.groupingBy(Employee::getDepartmentId, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= 2)
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .map(entry -> departmentNames.getOrDefault(entry.getKey(), "부서 미확인"))
                .toList();

        List<String> endingSoonProjectNames = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS)
                .filter(project -> {
                    LocalDate endDate = project.getPeriod().endDate();
                    return endDate != null
                            && endDate.isAfter(weekEnd)
                            && !endDate.isAfter(weekEnd.plusDays(7));
                })
                .sorted(Comparator.comparing(project -> project.getPeriod().endDate()))
                .limit(3)
                .map(Project::getName)
                .toList();

        List<String> noAssignmentProjectNames = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS)
                .filter(project -> projectAssignmentRepository.findOverlappingAssignments(project.getIdOrThrow(), weekEnd, weekEnd).isEmpty())
                .limit(3)
                .map(Project::getName)
                .toList();

        return new WeeklyReportSnapshot.RiskSection(
                endingSoonProjectNames,
                noAssignmentProjectNames,
                onLeaveDepartmentNames
        );
    }
}
