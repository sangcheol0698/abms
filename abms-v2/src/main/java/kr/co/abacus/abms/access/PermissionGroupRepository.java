package kr.co.abacus.abms.access;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {

    List<PermissionGroup> findAllByOrderByGroupTypeAscNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

}
