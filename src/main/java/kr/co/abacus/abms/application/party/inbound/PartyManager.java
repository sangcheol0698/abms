package kr.co.abacus.abms.application.party.inbound;

import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyUpdateRequest;

public interface PartyManager {

    Party findById(Long partyId);

    Party create(PartyCreateRequest request);

    Party update(Long partyId, PartyUpdateRequest request);

    void delete(Long partyId);

}
