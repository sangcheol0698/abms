package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRevenuePlanRepository extends JpaRepository<ProjectRevenuePlan, Long> {

    List<ProjectRevenuePlan> findAllByProjectIdOrderBySequenceAsc(Long projectId);

    boolean existsByProjectIdAndSequence(Long projectId, int sequence);

    List<ProjectRevenuePlan> findAllByIssuedFalseAndRevenueDateBetween(LocalDate from, LocalDate to);

    boolean existsByProjectIdAndIssuedTrue(Long projectId);

    List<ProjectRevenuePlan> findAllByProjectIdIn(java.util.Collection<Long> projectIds);

    boolean existsByProjectIdAndSequenceAndIdNot(Long projectId, int sequence, Long id);

    @Query("""
            select r from ProjectRevenuePlan r
            where r.issued = true and r.revenueDate between :from and :to
            """)
    List<ProjectRevenuePlan> findIssuedBetween(LocalDate from, LocalDate to);

    @Query("""
            select r from ProjectRevenuePlan r
            where r.projectId = :projectId and r.issued = true and r.revenueDate between :from and :to
            """)
    List<ProjectRevenuePlan> findIssuedBetween(Long projectId, LocalDate from, LocalDate to);

    @Query("""
            select r from ProjectRevenuePlan r
            where r.issued = false and r.revenueDate between :from and :to and r.projectId in :projectIds
            order by r.revenueDate asc
            """)
    List<ProjectRevenuePlan> findUnissuedBetween(Collection<Long> projectIds, LocalDate from, LocalDate to);

    @Query("select coalesce(max(r.sequence), 0) from ProjectRevenuePlan r where r.projectId = :projectId")
    int findMaxSequence(Long projectId);

}
