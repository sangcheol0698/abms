package kr.co.abacus.abms.department;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByParentId(Long parentId);

    boolean existsByCode(String code);

    List<Department> findAllBySiteId(Long siteId);

    boolean existsBySiteId(Long siteId);

}
