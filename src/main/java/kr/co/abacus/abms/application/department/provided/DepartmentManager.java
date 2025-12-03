package kr.co.abacus.abms.application.department.provided;

import java.util.UUID;

public interface DepartmentManager {

    void assignTeamLeader(UUID departmentId, UUID leaderEmployeeId);

}
