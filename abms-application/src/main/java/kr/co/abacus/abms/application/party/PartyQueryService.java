package kr.co.abacus.abms.application.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.party.inbound.PartyFinder;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PartyQueryService implements PartyFinder {

    private final PartyRepository partyRepository;

    @Override
    public Page<Party> getParties(Pageable pageable, @Nullable String name) {
        return partyRepository.search(pageable, name);
    }

    public String getPartyName(Long partyId) {
        return partyRepository.findById(partyId)
                .map(Party::getName)
                .orElseThrow(() -> new PartyNotFoundException("존재하지 않는 협력사입니다: " + partyId));
    }

}
