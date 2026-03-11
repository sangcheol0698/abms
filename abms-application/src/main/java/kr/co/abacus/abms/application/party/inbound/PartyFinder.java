package kr.co.abacus.abms.application.party.inbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.party.dto.PartyListItem;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.dto.PartySearchCondition;

public interface PartyFinder {

    Page<PartyListItem> getParties(Pageable pageable, PartySearchCondition condition);

    PartyOverviewSummary getOverviewSummary(PartySearchCondition condition);

}
