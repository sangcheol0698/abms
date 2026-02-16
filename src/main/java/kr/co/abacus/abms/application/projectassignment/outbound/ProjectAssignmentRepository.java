package kr.co.abacus.abms.application.projectassignment.outbound;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByProjectId(Long projectId);


    // 해당 월(monthStart ~ monthEnd)에 하루라도 투입 기간이 겹치는(Active) 할당 목록 조회
    @Query("SELECT pa FROM ProjectAssignment pa " +
        "WHERE pa.period.startDate <= :monthEnd " +
        "AND (pa.period.endDate IS NULL OR pa.period.endDate >= :monthStart)")
    List<ProjectAssignment> findActiveAssignments(@Param("monthStart") LocalDate monthStart,
                                                  @Param("monthEnd") LocalDate monthEnd);
}