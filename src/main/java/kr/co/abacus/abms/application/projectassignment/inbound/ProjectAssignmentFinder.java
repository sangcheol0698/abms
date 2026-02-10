package kr.co.abacus.abms.application.projectassignment.inbound;

import java.util.List;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public interface ProjectAssignmentFinder {

    List<ProjectAssignment> findByProjectId(Long projectId);
}
