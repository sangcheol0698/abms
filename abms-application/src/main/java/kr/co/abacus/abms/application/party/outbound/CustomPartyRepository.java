package kr.co.abacus.abms.application.party.outbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.party.dto.PartyListItem;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.dto.PartySearchCondition;

public interface CustomPartyRepository {

    Page<PartyListItem> search(Pageable pageable, PartySearchCondition condition);

    PartyOverviewSummary summarize(PartySearchCondition condition);

}
