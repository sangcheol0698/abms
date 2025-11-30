package kr.co.abacus.abms.application.party;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.party.provided.PartyFinder;
import kr.co.abacus.abms.application.party.required.PartyRepository;
import kr.co.abacus.abms.domain.party.Party;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PartyQueryService implements PartyFinder {

    private final PartyRepository partyRepository;

    @Override
    public Page<Party> getParties(Pageable pageable, String name) {
        return partyRepository.search(pageable, name);
    }

    public String getPartyId(UUID partyId) {
        return partyRepository.findById(partyId).map(Party::getName).orElse(null);
    }

}
