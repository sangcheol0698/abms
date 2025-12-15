package kr.co.abacus.abms.application.department.inbound;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentManager {

    Department assignTeamLeader(UUID departmentId, UUID leaderEmployeeId);

}
