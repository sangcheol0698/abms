package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.web.Ui;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.notification.NotificationService;
import kr.co.abacus.abms.notification.NotificationType;

/**
 * 놓치기 쉬운 일을 매일 알린다.
 * <ul>
 *   <li>청구일 임박: 미발행 매출의 청구일 3일 전 → 주관 부서장·PM</li>
 *   <li>청구 기한 경과: 미발행인 채 청구일이 지남 (다음 날, 이후 7일마다) → 주관 부서장·PM</li>
 *   <li>프로젝트 종료 임박: 진행 중 프로젝트 종료 14일 전 → 주관 부서장·PM</li>
 *   <li>투입 종료 임박: 투입 종료 14일 전 → 본인·소속 부서장 (다음 배치 준비)</li>
 * </ul>
 * 같은 날 다시 실행돼도 같은 알림은 한 번만 보낸다.
 */
@Service
@Transactional
public class ProjectReminderService {

    static final int BILLING_LEAD_DAYS = 3;
    static final int ENDING_LEAD_DAYS = 14;
    static final int OVERDUE_REPEAT_DAYS = 7;

    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public ProjectReminderService(ProjectRevenuePlanRepository revenuePlanRepository, ProjectRepository projectRepository,
                                  ProjectAssignmentRepository assignmentRepository, DepartmentRepository departmentRepository,
                                  EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.revenuePlanRepository = revenuePlanRepository;
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    /** @return 보낸 알림 수 */
    public int run(LocalDate today) {
        LocalDateTime since = today.atStartOfDay();
        int sent = 0;

        // 청구일 임박·기한 경과: 최근 1년 안의 미발행 매출만 본다.
        List<ProjectRevenuePlan> unissued = revenuePlanRepository.findAllByIssuedFalseAndRevenueDateBetween(today.minusYears(1),
                today.plusDays(BILLING_LEAD_DAYS));
        Map<Long, Project> projects = projectRepository.findAllById(unissued.stream().map(ProjectRevenuePlan::getProjectId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Project::id, Function.identity()));
        for (ProjectRevenuePlan plan : unissued) {
            Project project = projects.get(plan.getProjectId());
            if (project == null || project.getStatus().isClosed()) {
                continue;
            }
            long overdueDays = ChronoUnit.DAYS.between(plan.getRevenueDate(), today);
            String label = project.getName() + " " + plan.getSequence() + "차 " + Ui.won(plan.getAmount()) + "원";
            if (plan.getRevenueDate().equals(today.plusDays(BILLING_LEAD_DAYS))) {
                sent += notifyProjectOwners(project, NotificationType.INFO, "청구일 " + BILLING_LEAD_DAYS + "일 전: " + label,
                        "청구일 " + Ui.date(plan.getRevenueDate()) + " · 세금계산서 발행을 준비하세요.", since);
            } else if (overdueDays >= 1 && overdueDays % OVERDUE_REPEAT_DAYS == 1) {
                sent += notifyProjectOwners(project, NotificationType.WARNING, "미발행 매출 " + overdueDays + "일 경과: " + label,
                        "청구일 " + Ui.date(plan.getRevenueDate()) + "이 지났습니다. 발행했다면 프로젝트에서 발행 처리하세요.", since);
            }
        }

        for (Project project : projectRepository.findAllByStatusAndPeriodEndDate(ProjectStatus.IN_PROGRESS, today.plusDays(ENDING_LEAD_DAYS))) {
            sent += notifyProjectOwners(project, NotificationType.INFO, "프로젝트 종료 " + ENDING_LEAD_DAYS + "일 전: " + project.getName(),
                    "종료일 " + Ui.date(project.getPeriod().endDate()) + " · 남은 청구와 투입 종료를 확인하세요.", since);
        }

        for (ProjectAssignment assignment : assignmentRepository.findAllByPeriodEndDate(today.plusDays(ENDING_LEAD_DAYS))) {
            Project project = projectRepository.findById(assignment.getProjectId()).orElse(null);
            Employee employee = employeeRepository.findByIdAndDeletedFalse(assignment.getEmployeeId()).orElse(null);
            if (project == null || employee == null || employee.isResigned()) {
                continue;
            }
            String title = "투입 종료 " + ENDING_LEAD_DAYS + "일 전: " + employee.getName() + " · " + project.getName();
            String description = "종료일 " + Ui.date(assignment.getPeriod().endDate()) + " · 다음 배치를 준비하세요.";
            Set<Long> recipients = new LinkedHashSet<>();
            recipients.add(employee.id());
            Long leader = leaderOf(employee.getDepartmentId());
            if (leader != null) {
                recipients.add(leader);
            }
            for (Long recipient : recipients) {
                sent += notificationService.notifyEmployeeOnce(recipient, NotificationType.INFO, title, description, "/employees/" + employee.id(), since) ? 1 : 0;
            }
        }
        return sent;
    }

    /** 프로젝트 담당: 주관 부서장 + 현재 PM */
    private int notifyProjectOwners(Project project, NotificationType type, String title, String description, LocalDateTime since) {
        Set<Long> recipients = new LinkedHashSet<>();
        Long leader = leaderOf(project.getLeadDepartmentId());
        if (leader != null) {
            recipients.add(leader);
        }
        LocalDate today = since.toLocalDate();
        assignmentRepository.findAllByProjectIdOrderByPeriodStartDateAsc(project.id()).stream()
                .filter(a -> a.getRole() == AssignmentRole.PM && a.isActiveOn(today))
                .forEach(a -> recipients.add(a.getEmployeeId()));
        int sent = 0;
        for (Long employeeId : recipients) {
            sent += notificationService.notifyEmployeeOnce(employeeId, type, title, description, "/projects/" + project.id(), since) ? 1 : 0;
        }
        return sent;
    }

    private @Nullable Long leaderOf(Long departmentId) {
        return departmentRepository.findById(departmentId).map(Department::getLeaderEmployeeId).orElse(null);
    }

}
