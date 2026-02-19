package kr.co.abacus.abms.application.department;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.DepartmentLeaderDetail;
import kr.co.abacus.abms.application.department.dto.DepartmentProjection;
import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;
import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DepartmentQueryService implements DepartmentFinder {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public Department find(Long id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DepartmentNotFoundException("존재하지 않는 부서입니다: " + id));
    }

    @Override
    public Page<EmployeeSummary> getEmployees(Long departmentId, @Nullable String name, Pageable pageable) {
        // 부서 존재 여부 확인
        find(departmentId);

        // EmployeeRepository의 검색 기능 재사용 (정렬 로직 포함)
        EmployeeSearchCondition searchRequest = new EmployeeSearchCondition(
                name,
                null, // positions
                null, // types
                null, // statuses
                null, // grades
                departmentId != null ? List.of(departmentId) : null
        );

        return employeeRepository.search(searchRequest, pageable);
    }

    @Override
    public DepartmentDetail findDetail(Long departmentId) {
        return departmentRepository.findDetail(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("존재하지 않는 부서입니다: " + departmentId));
    }

    @Cacheable("organizationChart")
    @Override
    public List<OrganizationChartDetail> getOrganizationChart() {
        List<DepartmentProjection> projections = departmentRepository.findAllDepartmentProjections();

        Map<Long, List<DepartmentProjection>> childrenMap = groupByParentId(projections);

        List<DepartmentProjection> rootDepartments = findRootDepartments(projections);

        return rootDepartments.stream()
                .map(root -> mapToOrganizationChartInfo(root, childrenMap))
                .toList();
    }

    @CacheEvict(value = "organizationChart", allEntries = true)
    public void clearOrganizationChartCache() {
        log.info("[부서 계층 구조 캐시 초기화]");
    }

    private Map<Long, List<DepartmentProjection>> groupByParentId(List<DepartmentProjection> projections) {
        return projections.stream()
                .filter(d -> d.parentId() != null)
                .collect(Collectors.groupingBy(DepartmentProjection::parentId));
    }

    private List<DepartmentProjection> findRootDepartments(List<DepartmentProjection> projections) {
        return projections.stream()
                .filter(d -> d.parentId() == null)
                .toList();
    }

    private OrganizationChartDetail mapToOrganizationChartInfo(
            DepartmentProjection current,
            Map<Long, List<DepartmentProjection>> childrenMap) {
        List<DepartmentProjection> childrenList = childrenMap.getOrDefault(current.departmentId(),
                Collections.emptyList());

        List<OrganizationChartDetail> children = childrenList.stream()
                .sorted(Comparator.comparing(DepartmentProjection::departmentName))
                .map(child -> mapToOrganizationChartInfo(child, childrenMap))
                .toList();

        return new OrganizationChartDetail(
                current.departmentId(),
                current.departmentName(),
                current.departmentCode(),
                current.departmentType(),
                current.leaderEmployeeId() != null
                        && current.leaderEmployeeName() != null
                        && current.leaderEmployeePosition() != null
                                ? new DepartmentLeaderDetail(
                                current.leaderEmployeeId(),
                                current.leaderEmployeeName(),
                                current.leaderEmployeePosition()) : null,
                current.employeeCount(),
                children);
    }

}
