package kr.co.abacus.abms.application.commoncode;

import static org.mockito.Mockito.*;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import kr.co.abacus.abms.application.commoncode.outbound.CommonCodeDetailRepository;
import kr.co.abacus.abms.domain.commoncode.CommonCodeGroup;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("공통코드 서비스 캐시 통합 테스트")
class CommonCodeServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CommonCodeService commonCodeService;

    @MockitoSpyBean
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private CacheManager cacheManager;

    private final String groupCode = "G001";
    private final String code = "C001";

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        Objects.requireNonNull(cacheManager.getCache("commonCode")).clear();

        // 테스트 데이터 저장
        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group A")
                .description("Group A description")
                .build();

        entityManager.persist(group);
        flushAndClear();
    }

    @Test
    @DisplayName("그룹 코드로 조회 시 첫 번째만 리포지토리를 호출하고 두 번째는 캐시를 사용한다")
    void findByGroupCode_Cache() {
        // when
        commonCodeService.findByGroupCode(groupCode);
        commonCodeService.findByGroupCode(groupCode);

        // then
        verify(commonCodeDetailRepository, times(1)).findByGroupCode(groupCode);
    }

    @Test
    @DisplayName("상세 코드로 조회 시 첫 번째만 리포지토리를 호출하고 두 번째는 캐시를 사용한다")
    void findByCode_Cache() {
        // when
        commonCodeService.findByCode(groupCode, code);
        commonCodeService.findByCode(groupCode, code);

        // then
        verify(commonCodeDetailRepository, times(1)).findByCode(groupCode, code);
    }

    @Test
    @DisplayName("clearCache 호출 시 해당 그룹 코드의 캐시가 삭제된다")
    void clearCache_RemovesCache() {
        // given
        commonCodeService.findByGroupCode(groupCode); // 캐시 채우기
        verify(commonCodeDetailRepository, times(1)).findByGroupCode(groupCode);

        // when
        commonCodeService.clearCache(groupCode);

        // then
        commonCodeService.findByGroupCode(groupCode);
        verify(commonCodeDetailRepository, times(2)).findByGroupCode(groupCode);
    }
}
