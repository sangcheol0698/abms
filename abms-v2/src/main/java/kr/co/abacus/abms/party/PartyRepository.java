package kr.co.abacus.abms.party;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<Party> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Party> findAllByOrderByNameAsc();

}
