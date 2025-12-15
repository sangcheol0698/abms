package kr.co.abacus.abms.application.department;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.dto.EmployeeModel;
import kr.co.abacus.abms.application.department.dto.LeaderModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
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
    public List<Department> findAll() {
        return departmentRepository.findAllByDeletedFalse();
    }

    @Override
    public List<Department> findAllByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return departmentRepository.findAllByIdInAndDeletedFalse(ids);
    }

    @Override
    public OrganizationChartModel getOrganizationChart() {
        List<Department> allDepartments = departmentRepository.findAllByDeletedFalseWithChildren();
        Map<UUID, Long> employeeCountMap = departmentRepository.getDepartmentHeadcounts();

        Map<UUID, Employee> leadersMap = getLeadersMap(allDepartments);

        Department root = findRoot(allDepartments);

        return buildRecursiveChart(root, (department, children) -> {
            LeaderModel leaderModel = getLeaderModel(department, leadersMap);

            int employeeCount = employeeCountMap.getOrDefault(department.getId(), 0L).intValue();

            return new OrganizationChartModel(
                    department.getId(),
                    department.getName(),
                    department.getCode(),
                    department.getType(),
                    leaderModel,
                    employeeCount,
                    children);
        });
    }

    @Override
    public OrganizationChartWithEmployeesModel getOrganizationChartWithEmployees() {
        List<Department> allDepartments = departmentRepository.findAllByDeletedFalseWithChildren();
        Map<UUID, Employee> leadersMap = getLeadersMap(allDepartments);
        Map<UUID, List<Employee>> employeesByDeptId = getEmployeesByDeptId(allDepartments);

        Department root = findRoot(allDepartments);

        return buildRecursiveChart(root, (department, children) -> {
            LeaderModel leaderModel = getLeaderModel(department, leadersMap);
            List<EmployeeModel> employeeModels = employeesByDeptId.getOrDefault(department.getId(), List.of()).stream()
                    .map(e -> new EmployeeModel(e.getId(), e.getName(), e.getPosition()))
                    .toList();

            return new OrganizationChartWithEmployeesModel(
                    department.getId(),
                    department.getName(),
                    department.getCode(),
                    department.getType(),
                    leaderModel,
                    employeeModels,
                    children);
        });
    }

    @Override
    public Page<EmployeeSummary> getEmployees(UUID departmentId, String name, Pageable pageable) {
        // 부서 존재 여부 확인
        find(departmentId);

        // EmployeeRepository의 검색 기능 재사용 (정렬 로직 포함)
        EmployeeSearchCondition searchRequest = new EmployeeSearchCondition(
                name,
                null, // positions
                null, // types
                null, // statuses
                null, // grades
                departmentId != null ? List.of(departmentId) : null);

        return employeeRepository.search(searchRequest, pageable);
    }

    private Department findRoot(List<Department> allDepartments) {
        return allDepartments.stream()
                .filter(Department::isRoot)
                .findFirst()
                .orElseThrow(() -> new DepartmentNotFoundException("최상위 부서가 존재하지 않습니다"));
    }

    private Map<UUID, Employee> getLeadersMap(List<Department> allDepartments) {
        List<UUID> leaderIds = allDepartments.stream()
                .map(Department::getLeaderEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return employeeRepository.findAllByIdInAndDeletedFalse(leaderIds).stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));
    }

    private Map<UUID, List<Employee>> getEmployeesByDeptId(List<Department> departments) {
        List<UUID> departmentIds = departments.stream().map(Department::getId).toList();
        return employeeRepository.findAllByDepartmentIdInAndDeletedFalse(departmentIds).stream()
                .collect(Collectors.groupingBy(Employee::getDepartmentId));
    }

    private @Nullable LeaderModel getLeaderModel(Department department, Map<UUID, Employee> leadersMap) {
        UUID leaderId = department.getLeaderEmployeeId();
        Employee leader = (leaderId == null) ? null : leadersMap.get(leaderId);
        return (leader == null) ? null
                : new LeaderModel(leader.getId(), leader.getName(), leader.getPosition(), leader.getAvatar());
    }

    private <T> T buildRecursiveChart(Department department, BiFunction<Department, List<T>, T> nodeFactory) {
        List<T> childrenModels = department.getChildren().stream()
                .map(child -> buildRecursiveChart(child, nodeFactory))
                .toList();

        return nodeFactory.apply(department, childrenModels);
    }

}