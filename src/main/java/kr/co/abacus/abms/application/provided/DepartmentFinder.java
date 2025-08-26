package kr.co.abacus.abms.application.provided;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentFinder {

    Department find(UUID id);

}
