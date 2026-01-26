package kr.co.abacus.abms.application.party.outbound;

import java.util.Optional;
import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.party.Party;

public interface PartyRepository extends Repository<Party, Long>, CustomPartyRepository {

    Party save(Party party);

    Optional<Party> findById(Long id);
    
    Optional<Party> findByNameAndDeletedFalse(String name);

    List<Party> findAllByDeletedFalse();

}
