package kr.co.abacus.abms.application.department.inbound;

import java.util.UUID;

public interface DepartmentManager {

    UUID assignTeamLeader(UUID departmentId, UUID leaderEmployeeId);

}
