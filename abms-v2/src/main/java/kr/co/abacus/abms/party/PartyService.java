package kr.co.abacus.abms.party;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.party.Party.PartyInfo;
import kr.co.abacus.abms.project.ProjectRepository;

@Service
@Transactional
public class PartyService {

    private static final java.util.Comparator<PartyContact> CONTACT_ORDER = java.util.Comparator
            .comparing(PartyContact::isPrimary).reversed()
            .thenComparing(PartyContact::getRole)
            .thenComparing(PartyContact::getName);

    private final PartyRepository partyRepository;
    private final ProjectRepository projectRepository;
    private final PartyContactRepository contactRepository;

    public PartyService(PartyRepository partyRepository, ProjectRepository projectRepository, PartyContactRepository contactRepository) {
        this.contactRepository = contactRepository;
        this.partyRepository = partyRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public Page<Party> search(@Nullable String keyword, Pageable pageable) {
        return partyRepository.findAllByNameContainingIgnoreCase(keyword == null ? "" : keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Party> all() {
        return partyRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Party get(Long id) {
        return partyRepository.findById(id).orElseThrow(() -> NotFoundException.of("협력사", id));
    }

    @Transactional(readOnly = true)
    public long projectCount(Long partyId) {
        return projectRepository.countByPartyId(partyId);
    }

    /** 협력사별 프로젝트 수 (한 번의 집계 쿼리) */
    @Transactional(readOnly = true)
    public java.util.Map<Long, Long> projectCounts(java.util.Collection<Long> partyIds) {
        if (partyIds.isEmpty()) {
            return java.util.Map.of();
        }
        return projectRepository.countGroupByPartyId(partyIds).stream()
                .collect(java.util.stream.Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    /** 담당자: 대표 담당자 → 역할 → 이름 순 */
    @Transactional(readOnly = true)
    public List<PartyContact> contacts(Long partyId) {
        return contactRepository.findAllByPartyId(partyId).stream().sorted(CONTACT_ORDER).toList();
    }

    /** 협력사별 대표 담당자 (대표 지정이 없으면 정렬상 첫 번째) */
    @Transactional(readOnly = true)
    public java.util.Map<Long, PartyContact> primaryContacts(java.util.Collection<Long> partyIds) {
        if (partyIds.isEmpty()) {
            return java.util.Map.of();
        }
        return contactRepository.findAllByPartyIdIn(partyIds).stream().sorted(CONTACT_ORDER)
                .collect(java.util.stream.Collectors.toMap(PartyContact::getPartyId, c -> c, (first, other) -> first));
    }

    @Transactional(readOnly = true)
    public PartyContact contact(Long partyId, Long contactId) {
        return contactRepository.findById(contactId).filter(c -> c.getPartyId().equals(partyId))
                .orElseThrow(() -> NotFoundException.of("담당자", contactId));
    }

    public PartyContact addContact(Long partyId, PartyContact.ContactInfo info) {
        get(partyId);
        PartyContact contact = contactRepository.save(PartyContact.create(partyId, info));
        applyPrimary(contact, info.primary());
        return contact;
    }

    public void updateContact(Long partyId, Long contactId, PartyContact.ContactInfo info) {
        PartyContact contact = contact(partyId, contactId);
        contact.update(info);
        applyPrimary(contact, info.primary());
    }

    public void deleteContact(Long partyId, Long contactId, Long accountId) {
        contact(partyId, contactId).softDelete(accountId);
    }

    /** 대표 담당자는 협력사당 한 명: 새로 지정하면 기존 대표를 해제한다. */
    private void applyPrimary(PartyContact contact, boolean primary) {
        if (primary) {
            contactRepository.findAllByPartyId(contact.getPartyId()).stream()
                    .filter(c -> c.isPrimary() && !c.id().equals(contact.id()))
                    .forEach(c -> c.markPrimary(false));
        }
        contact.markPrimary(primary);
    }

    public Party create(PartyInfo info) {
        if (partyRepository.existsByName(info.name().trim())) {
            throw new BusinessException("이미 등록된 협력사명입니다: " + info.name());
        }
        return partyRepository.save(Party.create(info));
    }

    public void update(Long id, PartyInfo info) {
        Party party = get(id);
        if (partyRepository.existsByNameAndIdNot(info.name().trim(), id)) {
            throw new BusinessException("이미 등록된 협력사명입니다: " + info.name());
        }
        party.update(info);
    }

    public void delete(Long id, Long accountId) {
        Party party = get(id);
        if (projectRepository.existsByPartyId(id)) {
            throw new BusinessException("프로젝트가 연결된 협력사는 삭제할 수 없습니다.");
        }
        party.softDelete(accountId);
    }

}
