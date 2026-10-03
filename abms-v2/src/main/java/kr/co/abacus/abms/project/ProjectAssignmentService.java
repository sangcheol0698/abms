package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.notification.NotificationService;
import kr.co.abacus.abms.notification.NotificationType;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로젝트 투입 인력 관리.
 */
@Service
@Transactional
public class ProjectAssignmentService {

    private final ProjectService projectService;
    private final ProjectAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public ProjectAssignmentService(ProjectService projectService, ProjectAssignmentRepository assignmentRepository,
                                    EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.projectService = projectService;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ProjectAssignment> assignments(Long projectId) {
        return assignmentRepository.findAllByProjectIdOrderByPeriodStartDateAsc(projectId);
    }

    @Transactional(readOnly = true)
    public List<ProjectAssignment> assignmentsOfEmployee(Long employeeId) {
        return assignmentRepository.findAllByEmployeeIdOrderByPeriodStartDateDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public ProjectAssignment get(Long projectId, Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .filter(a -> a.getProjectId().equals(projectId))
                .orElseThrow(() -> NotFoundException.of("투입 정보", assignmentId));
    }

    public ProjectAssignment assign(LoginUser user, Long projectId, Long employeeId, @Nullable AssignmentRole role,
                                    LocalDate startDate, @Nullable LocalDate endDate) {
        Project project = projectService.getForWrite(user, projectId);
        Employee employee = employee(employeeId);
        checkOverlap(employeeId, startDate, endDate, -1L);
        ProjectAssignment assignment = assignmentRepository.save(
                ProjectAssignment.assign(project, employee, role, ProjectAssignment.periodOf(startDate, endDate)));
        notificationService.notifyEmployee(employeeId, NotificationType.INFO,
                "프로젝트 투입: " + project.getName(),
                startDate + " ~ " + (endDate == null ? "" : endDate) + " 기간으로 투입되었습니다.",
                "/projects/" + projectId);
        return assignment;
    }

    public void update(LoginUser user, Long projectId, Long assignmentId, Long employeeId, @Nullable AssignmentRole role,
                       LocalDate startDate, @Nullable LocalDate endDate) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectAssignment assignment = get(projectId, assignmentId);
        checkOverlap(employeeId, startDate, endDate, assignmentId);
        assignment.update(project, employee(employeeId), role, ProjectAssignment.periodOf(startDate, endDate));
    }

    public void delete(LoginUser user, Long projectId, Long assignmentId) {
        projectService.getForWrite(user, projectId);
        get(projectId, assignmentId).softDelete(user.accountId());
    }

    /**
     * 투입 M/M은 투입 기간 전체를 1.0으로 계산하므로, 동일 직원은 프로젝트와 관계없이 기간이 겹치게 투입될 수 없다.
     * (겹치면 같은 원가가 여러 프로젝트에 중복 배분된다)
     */
    private void checkOverlap(Long employeeId, LocalDate startDate, @Nullable LocalDate endDate, Long excludeId) {
        LocalDate to = endDate == null ? LocalDate.of(9999, 12, 31) : endDate;
        assignmentRepository.findOverlappingOfEmployee(employeeId, startDate, to, excludeId).stream().findFirst()
                .ifPresent(overlap -> {
                    Period period = overlap.getPeriod();
                    throw new BusinessException("이미 같은 기간에 다른 투입이 있는 직원입니다. ("
                            + period.startDate() + " ~ " + (period.endDate() == null ? "" : period.endDate()) + ")");
                });
    }

    private Employee employee(Long employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId).orElseThrow(() -> NotFoundException.of("직원", employeeId));
    }

}
