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

    private final PartyRepository partyRepository;
    private final ProjectRepository projectRepository;

    public PartyService(PartyRepository partyRepository, ProjectRepository projectRepository) {
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
