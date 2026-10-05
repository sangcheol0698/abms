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

    List<Project> findAllByStatusAndPeriodEndDate(ProjectStatus status, LocalDate endDate);

    long countByPartyId(Long partyId);

    @Query("select p from Project p where p.leadDepartmentId in :departmentIds or p.id in :projectIds")
    List<Project> findAllByLeadDepartmentIdInOrIdIn(@Param("departmentIds") Collection<Long> departmentIds,
                                                    @Param("projectIds") Collection<Long> projectIds);

    /**
     * 권한 범위(전체 / 주관 부서 / 참여 프로젝트) 안의 프로젝트. 전체 조회 후 메모리에서 거르지 않고 쿼리로 좁힌다.
     * 빈 IN 목록은 DB 마다 다르게 처리되므로 존재하지 않는 id(-1)로 채운다.
     */
    default List<Project> findAllInScope(boolean all, Collection<Long> departmentIds, Collection<Long> projectIds) {
        if (all) {
            return findAll();
        }
        if (departmentIds.isEmpty() && projectIds.isEmpty()) {
            return List.of();
        }
        return findAllByLeadDepartmentIdInOrIdIn(departmentIds.isEmpty() ? List.of(-1L) : departmentIds,
                projectIds.isEmpty() ? List.of(-1L) : projectIds);
    }

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
