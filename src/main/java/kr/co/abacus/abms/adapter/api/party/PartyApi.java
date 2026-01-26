package kr.co.abacus.abms.adapter.api.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.party.dto.PartyCreateApiRequest;
import kr.co.abacus.abms.adapter.api.party.dto.PartyResponse;
import kr.co.abacus.abms.adapter.api.party.dto.PartyUpdateApiRequest;
import kr.co.abacus.abms.application.party.inbound.PartyFinder;
import kr.co.abacus.abms.application.party.inbound.PartyManager;
import kr.co.abacus.abms.domain.party.Party;

@RequiredArgsConstructor
@RestController
public class PartyApi {

    private final PartyFinder partyFinder;
    private final PartyManager partyManager;

    @GetMapping("/api/parties")
    public PageResponse<PartyResponse> getParties(Pageable pageable, @RequestParam(required = false) String name) {
        Page<Party> parties = partyFinder.getParties(pageable, name);

        return PageResponse.of(parties.map(PartyResponse::from));
    }

    @GetMapping("/api/parties/{id}")
    public PartyResponse getParty(@PathVariable Long id) {
        Party party = partyManager.findById(id);
        return PartyResponse.from(party);
    }

    @PostMapping("/api/parties")
    @ResponseStatus(HttpStatus.CREATED)
    public PartyResponse create(@RequestBody PartyCreateApiRequest request) {
        Party party = partyManager.create(request.toDomainRequest());
        return PartyResponse.from(party);
    }

    @PutMapping("/api/parties/{id}")
    public PartyResponse update(@PathVariable Long id, @RequestBody PartyUpdateApiRequest request) {
        Party party = partyManager.update(id, request.toDomainRequest());
        return PartyResponse.from(party);
    }

    @DeleteMapping("/api/parties/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        partyManager.delete(id);
    }

}
