package kr.co.abacus.abms.application.department.inbound;

import kr.co.abacus.abms.application.auth.CurrentActor;

public interface DepartmentManager {

    Long assignLeader(Long departmentId, Long leaderEmployeeId);

    Long assignLeader(CurrentActor actor, Long departmentId, Long leaderEmployeeId);

}
