package kr.co.abacus.abms.application.party.inbound;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyUpdateRequest;

public interface PartyManager {

    Party findById(Long partyId);

    Party create(CurrentActor actor, PartyCreateRequest request);

    Party update(CurrentActor actor, Long partyId, PartyUpdateRequest request);

    void delete(CurrentActor actor, Long partyId);

}
