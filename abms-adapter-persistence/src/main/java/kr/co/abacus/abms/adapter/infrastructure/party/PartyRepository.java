package kr.co.abacus.abms.adapter.infrastructure.party;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.party.Party;

public interface PartyRepository
        extends Repository<Party, Long>,
        kr.co.abacus.abms.application.party.outbound.PartyRepository {
}
