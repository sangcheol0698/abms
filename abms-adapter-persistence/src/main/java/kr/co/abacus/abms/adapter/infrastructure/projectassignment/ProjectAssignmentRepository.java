package kr.co.abacus.abms.adapter.infrastructure.projectassignment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public interface ProjectAssignmentRepository
        extends JpaRepository<ProjectAssignment, Long>,
        kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository {

    @Override
    @Query("SELECT pa FROM ProjectAssignment pa " +
        "WHERE pa.period.startDate <= :monthEnd " +
        "AND (pa.period.endDate IS NULL OR pa.period.endDate >= :monthStart)")
    List<ProjectAssignment> findActiveAssignments(@Param("monthStart") LocalDate monthStart,
                                                  @Param("monthEnd") LocalDate monthEnd);

    @Override
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.projectId = :projectId AND pa.period.startDate <= :endOfMonth AND pa.period.endDate >= :startOfMonth")
    List<ProjectAssignment> findActiveAssignmentsByProjectId(@Param("projectId") Long projectId,
                                                             @Param("startOfMonth") LocalDate startOfMonth,
                                                             @Param("endOfMonth") LocalDate endOfMonth);

    @Override
    @Query("SELECT pa FROM ProjectAssignment pa " +
            "WHERE pa.projectId = :projectId " +
            "AND pa.deleted = false " +
            "AND pa.period.startDate <= :endDate " +
            "AND (pa.period.endDate IS NULL OR pa.period.endDate >= :startDate)")
    List<ProjectAssignment> findOverlappingAssignments(@Param("projectId") Long projectId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}
