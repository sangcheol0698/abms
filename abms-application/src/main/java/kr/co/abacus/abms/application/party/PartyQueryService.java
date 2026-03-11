package kr.co.abacus.abms.application.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.party.dto.PartyListItem;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.dto.PartySearchCondition;
import kr.co.abacus.abms.application.party.inbound.PartyFinder;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PartyQueryService implements PartyFinder {

    private final PartyRepository partyRepository;

    @Override
    public Page<PartyListItem> getParties(Pageable pageable, PartySearchCondition condition) {
        return partyRepository.search(pageable, condition);
    }

    @Override
    public PartyOverviewSummary getOverviewSummary(PartySearchCondition condition) {
        return partyRepository.summarize(condition);
    }

    public String getPartyName(Long partyId) {
        return partyRepository.findById(partyId)
                .map(Party::getName)
                .orElseThrow(() -> new PartyNotFoundException("존재하지 않는 협력사입니다: " + partyId));
    }

}
