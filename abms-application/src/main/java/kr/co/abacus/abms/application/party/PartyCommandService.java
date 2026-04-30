package kr.co.abacus.abms.application.party;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.party.inbound.PartyManager;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.DuplicatePartyNameException;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyDeletionDeniedException;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.party.PartyUpdateRequest;

@RequiredArgsConstructor
@Transactional
@Service
public class PartyCommandService implements PartyManager {

    private final PartyRepository partyRepository;
    private final ProjectRepository projectRepository;
    private final PartyAuthorizationValidator partyAuthorizationValidator;

    @Override
    @Transactional(readOnly = true)
    public Party findById(Long partyId) {
        return partyRepository.findByIdAndDeletedFalse(partyId)
                .orElseThrow(() -> new PartyNotFoundException("협력사를 찾을 수 없습니다: " + partyId));
    }

    @Override
    public Party create(CurrentActor actor, PartyCreateRequest request) {
        partyAuthorizationValidator.validateCreate(actor);
        validateDuplicateName(normalizeName(request.name()));
        Party party = Party.create(request);
        return partyRepository.save(party);
    }

    @Override
    public Party update(CurrentActor actor, Long partyId, PartyUpdateRequest request) {
        partyAuthorizationValidator.validateUpdate(actor);
        Party party = findById(partyId);
        String normalizedName = normalizeName(request.name());
        validateDuplicateNameForUpdate(party.getName(), normalizedName);
        party.update(request);
        return party;
    }

    @Override
    public void delete(CurrentActor actor, Long partyId) {
        partyAuthorizationValidator.validateDelete(actor);
        Party party = findById(partyId);
        validateNoProjects(partyId);
        party.softDelete(actor.accountId());
    }

    private void validateDuplicateName(String name) {
        if (partyRepository.existsByNameAndDeletedFalse(name)) {
            throw new DuplicatePartyNameException("이미 존재하는 협력사명입니다: " + name);
        }
    }

    private void validateDuplicateNameForUpdate(String currentName, String newName) {
        if (!currentName.equals(newName)) {
            validateDuplicateName(newName);
        }
    }

    private void validateNoProjects(Long partyId) {
        if (projectRepository.existsByPartyIdAndDeletedFalse(partyId)) {
            throw new PartyDeletionDeniedException("프로젝트가 연결된 협력사는 삭제할 수 없습니다: " + partyId);
        }
    }

    private String normalizeName(String name) {
        return java.util.Objects.requireNonNull(name).trim();
    }

}
