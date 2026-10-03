package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {

    List<ProjectAssignment> findAllByProjectIdOrderByPeriodStartDateAsc(Long projectId);

    List<ProjectAssignment> findAllByEmployeeIdOrderByPeriodStartDateDesc(Long employeeId);

    @Query("""
            select a from ProjectAssignment a
            where a.period.startDate <= :to and (a.period.endDate is null or a.period.endDate >= :from)
            """)
    List<ProjectAssignment> findOverlapping(LocalDate from, LocalDate to);

    @Query("""
            select a from ProjectAssignment a
            where a.projectId = :projectId
              and a.period.startDate <= :to and (a.period.endDate is null or a.period.endDate >= :from)
            """)
    List<ProjectAssignment> findOverlapping(Long projectId, LocalDate from, LocalDate to);

    /** 같은 프로젝트에서 같은 직원의 기간이 겹치는 투입이 있는지 (수정 중인 투입은 제외) */
    @Query("""
            select count(a) > 0 from ProjectAssignment a
            where a.projectId = :projectId and a.employeeId = :employeeId and a.id <> :excludeId
              and a.period.startDate <= :to and (a.period.endDate is null or a.period.endDate >= :from)
            """)
    boolean existsOverlap(Long projectId, Long employeeId, LocalDate from, LocalDate to, Long excludeId);

    @Query("""
            select distinct a.projectId from ProjectAssignment a
            where a.employeeId = :employeeId
              and a.period.startDate <= :date and (a.period.endDate is null or a.period.endDate >= :date)
            """)
    List<Long> findActiveProjectIds(Long employeeId, LocalDate date);

}
