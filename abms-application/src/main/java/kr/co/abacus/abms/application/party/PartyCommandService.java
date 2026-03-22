package kr.co.abacus.abms.application.party;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.party.inbound.PartyManager;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.party.PartyUpdateRequest;

@RequiredArgsConstructor
@Transactional
@Service
public class PartyCommandService implements PartyManager {

    private final PartyRepository partyRepository;
    private final PartyAuthorizationValidator partyAuthorizationValidator;

    @Override
    @Transactional(readOnly = true)
    public Party findById(Long partyId) {
        return partyRepository.findByIdAndDeletedFalse(partyId)
                .orElseThrow(() -> new PartyNotFoundException("협력사를 찾을 수 없습니다: " + partyId));
    }

    @Override
    public Party create(CurrentActor actor, PartyCreateRequest request) {
        partyAuthorizationValidator.validateCreate(actor);
        Party party = Party.create(request);
        return partyRepository.save(party);
    }

    @Override
    public Party update(CurrentActor actor, Long partyId, PartyUpdateRequest request) {
        partyAuthorizationValidator.validateUpdate(actor);
        Party party = findById(partyId);
        party.update(request);
        return party;
    }

    @Override
    public void delete(CurrentActor actor, Long partyId) {
        partyAuthorizationValidator.validateDelete(actor);
        Party party = findById(partyId);
        party.softDelete(null);
    }

}
