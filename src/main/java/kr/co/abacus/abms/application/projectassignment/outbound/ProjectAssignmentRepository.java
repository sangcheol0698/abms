package kr.co.abacus.abms.application.projectassignment.outbound;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByProjectId(Long projectId);
}