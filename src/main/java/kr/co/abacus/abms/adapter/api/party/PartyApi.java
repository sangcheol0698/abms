package kr.co.abacus.abms.adapter.api.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.party.dto.PartyResponse;
import kr.co.abacus.abms.application.party.inbound.PartyFinder;
import kr.co.abacus.abms.domain.party.Party;

@RequiredArgsConstructor
@RestController
public class PartyApi {

    private final PartyFinder partyFinder;

    @GetMapping("/api/parties")
    public PageResponse<PartyResponse> getParties(Pageable pageable, @RequestParam(required = false) String name) {
        Page<Party> parties = partyFinder.getParties(pageable, name);

        return PageResponse.of(parties.map(PartyResponse::from));
    }

}
