package kr.co.abacus.abms.application.department;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.dto.LeaderModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.employee.Employee;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DepartmentQueryService implements DepartmentFinder {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public Department find(UUID id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new DepartmentNotFoundException("존재하지 않는 부서입니다: " + id));
    }

    @Override
    public OrganizationChartModel getOrganizationChart() {
        List<Department> allDepartments = departmentRepository.findAllByDeletedFalse();

        Map<UUID, Employee> leadersMap = getLeadersMap(allDepartments);

        List<Department> roots = allDepartments.stream()
            .filter(Department::isRoot)
            .toList();

        if (roots.size() != 1) {
            throw new IllegalStateException("최상위 부서(root)가 하나가 아닙니다. 데이터 확인이 필요합니다.");
        }

        Department root = roots.getFirst();
        return buildOrganizationChartModel(root, leadersMap);
    }

    private OrganizationChartModel buildOrganizationChartModel(Department department, Map<UUID, Employee> leadersMap) {
        LeaderModel leaderModel = getLeaderModel(department, leadersMap);

        List<OrganizationChartModel> childrenModels = department.getChildren().stream()
            .map(child -> buildOrganizationChartModel(child, leadersMap))
            .toList();

        return new OrganizationChartModel(
            department.getId(),
            department.getName(),
            department.getCode(),
            department.getType(),
            leaderModel,
            childrenModels
        );
    }

    private Map<UUID, Employee> getLeadersMap(List<Department> allDepartments) {
        List<UUID> leaderIds = allDepartments.stream()
            .map(Department::getLeaderEmployeeId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (leaderIds.isEmpty()) {
            return Map.of();
        }

        return employeeRepository.findAllByIdInAndDeletedFalse(leaderIds).stream()
            .collect(Collectors.toMap(Employee::getId, employee -> employee));
    }

    private @Nullable LeaderModel getLeaderModel(Department department, Map<UUID, Employee> leadersMap) {
        UUID leaderId = department.getLeaderEmployeeId();
        Employee leader = (leaderId == null) ? null : leadersMap.get(leaderId);

        return (leader == null) ? null : new LeaderModel(leader.getId(), leader.getName(), leader.getPosition());
    }

}