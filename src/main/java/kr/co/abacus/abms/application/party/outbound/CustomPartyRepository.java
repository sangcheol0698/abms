package kr.co.abacus.abms.application.party.outbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.Party;

public interface CustomPartyRepository {

    Page<Party> search(Pageable pageable, @Nullable String name);

}
