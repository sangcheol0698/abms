package kr.co.abacus.abms.application.projectassignment.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public interface ProjectAssignmentRepository {

    ProjectAssignment save(ProjectAssignment projectAssignment);

    <S extends ProjectAssignment> List<S> saveAll(Iterable<S> projectAssignments);

    Optional<ProjectAssignment> findById(Long id);

    List<ProjectAssignment> findByProjectId(Long projectId);

    List<ProjectAssignment> findActiveAssignments(LocalDate monthStart, LocalDate monthEnd);

    List<ProjectAssignment> findActiveAssignmentsByProjectId(Long projectId, LocalDate startOfMonth, LocalDate endOfMonth);

    List<ProjectAssignment> findOverlappingAssignments(Long projectId, LocalDate startDate, LocalDate endDate);
}
