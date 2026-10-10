package kr.co.abacus.abms.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class SiteServiceTest {

    @Autowired
    private SiteService siteService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private Fixtures fixtures;

    @Test
    void 부서가_연결된_사업장은_삭제할_수_없다() {
        Department root = fixtures.department("회사", null);
        LoginUser admin = Fixtures.admin(fixtures.employee(root, "관리자"));
        Site site = siteRepository.save(Site.create(new Site.SiteInfo("본사", SiteType.HEADQUARTERS, null, null, null)));
        root.relocate(site.id());

        assertThatThrownBy(() -> siteService.delete(admin, site.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("부서가 연결된 사업장은 삭제할 수 없습니다.");
        assertThat(site.isDeleted()).isFalse();

        root.relocate(null);
        siteService.delete(admin, site.id());
        assertThat(site.isDeleted()).isTrue();
    }

}
