package kr.co.abacus.abms.application.party.provided;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.domain.party.Party;

public interface PartyFinder {

    Page<Party> getParties(Pageable pageable, String name);

}
