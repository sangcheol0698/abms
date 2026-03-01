package kr.co.abacus.abms.adapter.infrastructure.department;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentRepository
        extends Repository<Department, Long>,
        kr.co.abacus.abms.application.department.outbound.DepartmentRepository {
}
