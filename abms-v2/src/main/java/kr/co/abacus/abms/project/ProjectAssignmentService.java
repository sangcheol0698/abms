package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.ClosedMonthGuard;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeDeleting;
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
    private final ClosedMonthGuard closedMonthGuard;

    public ProjectAssignmentService(ProjectService projectService, ProjectAssignmentRepository assignmentRepository,
                                    EmployeeRepository employeeRepository, NotificationService notificationService,
                                    ClosedMonthGuard closedMonthGuard) {
        this.projectService = projectService;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
        this.closedMonthGuard = closedMonthGuard;
    }

    /** 삭제된 직원은 원가 집계에서 빠지므로, 투입 이력이 있는 직원은 삭제 대신 퇴사 처리해야 한다. */
    @EventListener
    public void onEmployeeDeleting(EmployeeDeleting event) {
        if (assignmentRepository.existsByEmployeeId(event.employeeId())) {
            throw new BusinessException("프로젝트 투입 이력이 있는 직원은 삭제할 수 없습니다. 퇴사 처리하세요.");
        }
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

    /** 전담(투입률 100%) 투입 */
    public ProjectAssignment assign(LoginUser user, Long projectId, Long employeeId, @Nullable AssignmentRole role,
                                    LocalDate startDate, @Nullable LocalDate endDate) {
        return assign(user, projectId, employeeId, role, startDate, endDate, ProjectAssignment.FULL_RATE);
    }

    public ProjectAssignment assign(LoginUser user, Long projectId, Long employeeId, @Nullable AssignmentRole role,
                                    LocalDate startDate, @Nullable LocalDate endDate, int allocationRate) {
        Project project = projectService.getForWrite(user, projectId);
        Employee employee = employee(employeeId);
        checkCapacity(employeeId, startDate, endDate, allocationRate, -1L);
        closedMonthGuard.checkOpen(startDate, endDate, "투입 등록");
        ProjectAssignment assignment = assignmentRepository.save(
                ProjectAssignment.assign(project, employee, role, ProjectAssignment.periodOf(startDate, endDate), allocationRate));
        notificationService.notifyEmployee(employeeId, NotificationType.INFO,
                "프로젝트 투입: " + project.getName(),
                startDate + " ~ " + (endDate == null ? "" : endDate) + " 기간으로 투입되었습니다."
                        + (allocationRate == ProjectAssignment.FULL_RATE ? "" : " (투입률 " + allocationRate + "%)"),
                "/projects/" + projectId);
        return assignment;
    }

    public void update(LoginUser user, Long projectId, Long assignmentId, Long employeeId, @Nullable AssignmentRole role,
                       LocalDate startDate, @Nullable LocalDate endDate, int allocationRate) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectAssignment assignment = get(projectId, assignmentId);
        checkCapacity(employeeId, startDate, endDate, allocationRate, assignmentId);
        checkChangedPeriodOpen(assignment, employeeId, startDate, endDate, allocationRate);
        assignment.update(project, employee(employeeId), role, ProjectAssignment.periodOf(startDate, endDate), allocationRate);
    }

    public void delete(LoginUser user, Long projectId, Long assignmentId) {
        projectService.getForWrite(user, projectId);
        ProjectAssignment assignment = get(projectId, assignmentId);
        closedMonthGuard.checkOpen(assignment.getPeriod().startDate(), assignment.getPeriod().endDate(), "투입 삭제");
        assignment.softDelete(user.accountId());
    }

    /** 투입 M/M이 달라지는 구간에 마감된 월이 있으면 막는다. (역할만 바꾸는 것은 허용) */
    private void checkChangedPeriodOpen(ProjectAssignment assignment, Long employeeId, LocalDate startDate, @Nullable LocalDate endDate,
                                        int allocationRate) {
        Period before = assignment.getPeriod();
        // 직원이나 투입률이 바뀌면 이전·이후 기간 전체의 M/M이 달라진다.
        if (!assignment.getEmployeeId().equals(employeeId) || assignment.getAllocationRate() != allocationRate) {
            closedMonthGuard.checkOpen(before.startDate(), before.endDate(), "투입 수정");
            closedMonthGuard.checkOpen(startDate, endDate, "투입 수정");
            return;
        }
        if (!before.startDate().equals(startDate)) {
            LocalDate from = before.startDate().isBefore(startDate) ? before.startDate() : startDate;
            LocalDate to = before.startDate().isBefore(startDate) ? startDate : before.startDate();
            closedMonthGuard.checkOpen(from, to.minusDays(1), "투입 수정");
        }
        LocalDate beforeEnd = before.endDate() == null ? LocalDate.MAX : before.endDate();
        LocalDate afterEnd = endDate == null ? LocalDate.MAX : endDate;
        if (!beforeEnd.equals(afterEnd)) {
            LocalDate earlier = beforeEnd.isBefore(afterEnd) ? beforeEnd : afterEnd;
            LocalDate later = beforeEnd.isBefore(afterEnd) ? afterEnd : beforeEnd;
            closedMonthGuard.checkOpen(earlier.plusDays(1), later.equals(LocalDate.MAX) ? null : later, "투입 수정");
        }
    }

    /**
     * 같은 직원의 투입률 합계는 어느 날짜에서도 100%를 넘을 수 없다. (넘으면 같은 원가가 여러 프로젝트에 중복 배분된다)
     * 동시 투입률은 투입이 시작되는 날에만 늘어나므로, 새 기간의 시작일과 그 안에서 시작하는 기존 투입의 시작일만 확인하면 된다.
     */
    private void checkCapacity(Long employeeId, LocalDate startDate, @Nullable LocalDate endDate, int allocationRate, Long excludeId) {
        LocalDate to = endDate == null ? LocalDate.of(9999, 12, 31) : endDate;
        List<ProjectAssignment> overlapping = assignmentRepository.findOverlappingOfEmployee(employeeId, startDate, to, excludeId);
        Stream.concat(Stream.of(startDate), overlapping.stream().map(a -> a.getPeriod().startDate()).filter(d -> d.isAfter(startDate)))
                .distinct()
                .forEach(date -> {
                    List<ProjectAssignment> active = overlapping.stream().filter(a -> a.isActiveOn(date)).toList();
                    int used = active.stream().mapToInt(ProjectAssignment::getAllocationRate).sum();
                    if (used + allocationRate > ProjectAssignment.FULL_RATE) {
                        Period period = active.getFirst().getPeriod();
                        throw new BusinessException("같은 기간 투입률 합계가 100%를 넘습니다. " + date + " 기준 다른 투입 " + used
                                + "% + 이번 투입 " + allocationRate + "% (예: " + period.startDate() + " ~ "
                                + (period.endDate() == null ? "" : period.endDate()) + ")");
                    }
                });
    }

    private Employee employee(Long employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId).orElseThrow(() -> NotFoundException.of("직원", employeeId));
    }

}
