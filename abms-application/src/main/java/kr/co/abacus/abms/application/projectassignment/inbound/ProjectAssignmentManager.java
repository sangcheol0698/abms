package kr.co.abacus.abms.application.projectassignment.inbound;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;

public interface ProjectAssignmentManager {

    ProjectAssignment create(ProjectAssignmentCreateRequest createRequest);
}
