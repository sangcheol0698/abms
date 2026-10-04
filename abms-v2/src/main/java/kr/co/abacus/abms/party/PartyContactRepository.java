package kr.co.abacus.abms.party;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyContactRepository extends JpaRepository<PartyContact, Long> {

    List<PartyContact> findAllByPartyId(Long partyId);

    List<PartyContact> findAllByPartyIdIn(Collection<Long> partyIds);

}
