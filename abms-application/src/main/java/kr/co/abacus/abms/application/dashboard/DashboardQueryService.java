package kr.co.abacus.abms.application.dashboard;

import java.time.LocalDate;

import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
import kr.co.abacus.abms.application.dashboard.inbound.DashboardFinder;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.project.ProjectStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService implements DashboardFinder {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public DashboardSummaryResponse getDashboardSummary() {
        int totalEmployeesCount = employeeRepository.count();
        int activeProjectsCount = projectRepository.countByStatus(ProjectStatus.IN_PROGRESS);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());
        int newEmployeesCount = employeeRepository.countByJoinDateBetween(startDate, endDate);

        int onLeaveEmployeesCount = employeeRepository.countByStatus(EmployeeStatus.ON_LEAVE);

        return new DashboardSummaryResponse(totalEmployeesCount, activeProjectsCount, newEmployeesCount, onLeaveEmployeesCount);
    }

}