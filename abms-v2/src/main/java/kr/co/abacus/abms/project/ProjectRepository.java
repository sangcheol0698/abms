package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    boolean existsByCode(String code);

    boolean existsByPartyId(Long partyId);

    long countByPartyId(Long partyId);

    /** 협력사별 프로젝트 수: [partyId, count] */
    @Query("select p.partyId, count(p) from Project p where p.partyId in :partyIds group by p.partyId")
    List<Object[]> countGroupByPartyId(@Param("partyIds") Collection<Long> partyIds);

    List<Project> findAllByPartyIdOrderByPeriodStartDateDesc(Long partyId);

    List<Project> findAllByLeadDepartmentIdInOrderByPeriodStartDateDesc(Collection<Long> departmentIds);

    @Query("""
            select p from Project p
            where p.period.startDate <= :to and (p.period.endDate is null or p.period.endDate >= :from)
            """)
    List<Project> findOverlapping(LocalDate from, LocalDate to);

    @Query("select count(p) from Project p where p.code like concat(:prefix, '%')")
    long countByCodePrefix(String prefix);

}
