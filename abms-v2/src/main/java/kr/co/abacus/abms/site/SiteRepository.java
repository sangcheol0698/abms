package kr.co.abacus.abms.site;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Site> findAllByOrderByNameAsc();

}
