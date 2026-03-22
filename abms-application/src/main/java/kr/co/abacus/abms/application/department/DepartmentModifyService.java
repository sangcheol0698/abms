package kr.co.abacus.abms.application.department;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.inbound.DepartmentManager;
import kr.co.abacus.abms.application.department.event.OrganizationChartInvalidationRequestedEvent;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;

@Validated
@RequiredArgsConstructor
@Transactional
@Service
public class DepartmentModifyService implements DepartmentManager {

    private final DepartmentRepository departmentRepository;
    private final DepartmentFinder departmentFinder;
    private final EmployeeFinder employeeFinder;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentActorPermissionSupport permissionSupport;

    @Override
    public Long assignLeader(Long departmentId, Long leaderEmployeeId) {
        return doAssignLeader(departmentId, leaderEmployeeId);
    }

    @Override
    public Long assignLeader(CurrentActor actor, Long departmentId, Long leaderEmployeeId) {
        validateCanAssignLeader(actor, departmentId);
        return doAssignLeader(departmentId, leaderEmployeeId);
    }

    private Long doAssignLeader(Long departmentId, Long leaderEmployeeId) {
        Department department = departmentFinder.find(departmentId);
        Employee employee = employeeFinder.find(leaderEmployeeId);

        department.assignLeader(employee.getIdOrThrow());

        Long id = departmentRepository.save(department).getIdOrThrow();

        requestOrganizationChartCacheInvalidation();

        return id;
    }

    private void validateCanAssignLeader(CurrentActor actor, Long departmentId) {
        permissionSupport.validateDepartmentAccess(
                actor,
                "employee.write",
                departmentId,
                "부서 리더 변경 권한 범위를 벗어났습니다."
        );
    }

    private void requestOrganizationChartCacheInvalidation() {
        eventPublisher.publishEvent(new OrganizationChartInvalidationRequestedEvent());
    }

}
