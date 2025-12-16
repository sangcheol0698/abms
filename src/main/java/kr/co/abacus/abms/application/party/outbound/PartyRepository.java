package kr.co.abacus.abms.application.party.outbound;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.party.Party;

public interface PartyRepository extends Repository<Party, Long>, CustomPartyRepository {

    Party save(Party party);

    Optional<Party> findById(UUID id);

}
