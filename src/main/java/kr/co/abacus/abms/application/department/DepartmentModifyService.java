package kr.co.abacus.abms.application.department;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.inbound.DepartmentManager;

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

    @Override
    public UUID assignTeamLeader(UUID departmentId, UUID leaderEmployeeId) {
        Department department = departmentFinder.find(departmentId);
        Employee employee = employeeFinder.find(leaderEmployeeId);

        department.assignTeamLeader(employee.getId());

        return departmentRepository.save(department).getId();
    }

}
