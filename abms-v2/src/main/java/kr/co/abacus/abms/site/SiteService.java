package kr.co.abacus.abms.site;

import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.site.Site.SiteInfo;

/**
 * 사업장 관리. 조직 구성의 일부로 보고 부서 관리 권한으로 쓰기를 허용한다.
 */
@Service
@Transactional
public class SiteService {

    private final SiteRepository siteRepository;
    private final AccessService accessService;
    private final ApplicationEventPublisher eventPublisher;

    public SiteService(SiteRepository siteRepository, AccessService accessService, ApplicationEventPublisher eventPublisher) {
        this.siteRepository = siteRepository;
        this.accessService = accessService;
        this.eventPublisher = eventPublisher;
    }

    /** 본사 → 지사 → … 유형 순, 같은 유형은 이름 순 */
    @Transactional(readOnly = true)
    public List<Site> all() {
        return siteRepository.findAllByOrderByNameAsc().stream()
                .sorted(Comparator.comparing(Site::getSiteType))
                .toList();
    }

    @Transactional(readOnly = true)
    public Site get(Long id) {
        return siteRepository.findById(id).orElseThrow(() -> NotFoundException.of("사업장", id));
    }

    @Transactional(readOnly = true)
    public @Nullable Site find(Long id) {
        return siteRepository.findById(id).orElse(null);
    }

    public Site create(LoginUser user, SiteInfo info) {
        accessService.require(user, PermissionCode.DEPARTMENT_WRITE);
        if (info.name() != null && siteRepository.existsByName(info.name().trim())) {
            throw new BusinessException("이미 등록된 사업장명입니다: " + info.name());
        }
        return siteRepository.save(Site.create(info));
    }

    public void update(LoginUser user, Long id, SiteInfo info) {
        accessService.require(user, PermissionCode.DEPARTMENT_WRITE);
        Site site = get(id);
        if (info.name() != null && siteRepository.existsByNameAndIdNot(info.name().trim(), id)) {
            throw new BusinessException("이미 등록된 사업장명입니다: " + info.name());
        }
        site.update(info);
    }

    public void delete(LoginUser user, Long id) {
        accessService.require(user, PermissionCode.DEPARTMENT_WRITE);
        Site site = get(id);
        eventPublisher.publishEvent(new SiteDeleting(id));
        site.softDelete(user.accountId());
    }

}
