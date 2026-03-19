package kr.co.abacus.abms.application.projectassignment.inbound;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentEndRequest;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentUpdateRequest;

public interface ProjectAssignmentManager {

    ProjectAssignment create(ProjectAssignmentCreateRequest createRequest);

    ProjectAssignment update(Long assignmentId, ProjectAssignmentUpdateRequest updateRequest);

    ProjectAssignment end(Long assignmentId, ProjectAssignmentEndRequest endRequest);
}
