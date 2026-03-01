package kr.co.abacus.abms.application.party.inbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.Party;

public interface PartyFinder {

    Page<Party> getParties(Pageable pageable, @Nullable String name);

}
