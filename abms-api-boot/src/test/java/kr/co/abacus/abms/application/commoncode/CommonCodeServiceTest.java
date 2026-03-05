package kr.co.abacus.abms.application.commoncode;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import kr.co.abacus.abms.application.commoncode.dto.CommonCodeInfo;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;
import kr.co.abacus.abms.domain.commoncode.CommonCodeGroup;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("공통코드 서비스 통합 테스트")
class CommonCodeServiceTest extends IntegrationTestBase {

    @Autowired
    private CommonCodeService commonCodeService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("commonCode")).clear();
    }

    @Test
    @DisplayName("그룹이 존재할 때 findByGroupCode는 상세 정보를 반환한다")
    void findByGroupCodeReturnsDetailWhenGroupExists() {
        String groupCode = "GROUP_A";
        String code = "A01";

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group A")
                .description("Group A description")
                .build();
        group.addDetail(code, "Code A01", 1);

        entityManager.persist(group);
        // detail은 cascade로 저장됨
        flushAndClear();

        List<CommonCodeInfo> commonCodeDetails = commonCodeService.findByGroupCode(groupCode);

        CommonCodeInfo found = commonCodeDetails.getFirst();
        assertThat(found).isNotNull();
        assertThat(found.groupCode()).isEqualTo(groupCode);
        assertThat(found.code()).isEqualTo(code);
        assertThat(found.name()).isEqualTo("Code A01");
    }

    @Test
    @DisplayName("그룹이 삭제되었을 때 findByGroupCode는 null을 반환한다")
    void findByGroupCodeReturnsNullWhenGroupDeleted() {
        String groupCode = "GROUP_B";
        String code = "B01";

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group B")
                .description("Group B description")
                .build();
        group.addDetail(code, "Code B01", 1);

        entityManager.persist(group);
        entityManager.flush();

        group.softDelete("tester");
        flushAndClear();

        List<CommonCodeInfo> commonCodeDetails = commonCodeService.findByGroupCode(groupCode);

        assertThat(commonCodeDetails).isEmpty();
    }

    @Test
    @DisplayName("상세 정보가 삭제되었을 때 findByGroupCode는 null을 반환한다")
    void findByGroupCodeReturnsNullWhenDetailDeleted() {
        String groupCode = "GROUP_C";
        String code = "C01";

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group C")
                .description("Group C description")
                .build();
        CommonCodeDetail detail = group.addDetail(code, "Code C01", 1);

        entityManager.persist(group);
        entityManager.flush();

        detail.softDelete("tester");
        flushAndClear();

        List<CommonCodeInfo> commonCodeDetails = commonCodeService.findByGroupCode(groupCode);

        assertThat(commonCodeDetails).isEmpty();
    }

    @Test
    @DisplayName("그룹 코드가 존재하지 않을 때 findByGroupCode는 null을 반환한다")
    void findByGroupCodeReturnsNullWhenGroupCodeMissing() {
        List<CommonCodeInfo> commonCodeDetails = commonCodeService.findByGroupCode("MISSING_GROUP");

        assertThat(commonCodeDetails).isEmpty();
    }

    @Test
    @DisplayName("코드가 존재할 때 findByCode는 상세 정보를 반환한다")
    void findByCodeReturnsDetailWhenCodeExists() {
        String groupCode = "GROUP_E";
        String code = "E01";

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group E")
                .description("Group E description")
                .build();
        group.addDetail(code, "Code E01", 1);

        entityManager.persist(group);
        flushAndClear();

        CommonCodeInfo found = commonCodeService.findByCode(groupCode, code);

        assertThat(found).isNotNull();
        assertThat(found.groupCode()).isEqualTo(groupCode);
        assertThat(found.code()).isEqualTo(code);
        assertThat(found.name()).isEqualTo("Code E01");
    }

    @Test
    @DisplayName("그룹만 삭제된 비정상 상태에서는 findByGroupCode가 빈 결과를 반환한다")
    void findByGroupCodeReturnsEmptyWhenOnlyGroupDeleted() {
        String groupCode = "GROUP_F";
        String code = "F01";

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group F")
                .description("Group F description")
                .build();
        group.addDetail(code, "Code F01", 1);

        entityManager.persist(group);
        flushAndClear();

        entityManager.createQuery("UPDATE CommonCodeGroup g SET g.deleted = true WHERE g.groupCode = :groupCode")
                .setParameter("groupCode", groupCode)
                .executeUpdate();
        flushAndClear();

        List<CommonCodeInfo> commonCodeDetails = commonCodeService.findByGroupCode(groupCode);

        assertThat(commonCodeDetails).isEmpty();
    }

    @Test
    @DisplayName("그룹만 삭제된 비정상 상태에서는 findByCode가 null을 반환한다")
    void findByCodeReturnsNullWhenOnlyGroupDeleted() {
        String groupCode = "GROUP_G";
        String code = "G01";

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode(groupCode)
                .groupName("Group G")
                .description("Group G description")
                .build();
        group.addDetail(code, "Code G01", 1);

        entityManager.persist(group);
        flushAndClear();

        entityManager.createQuery("UPDATE CommonCodeGroup g SET g.deleted = true WHERE g.groupCode = :groupCode")
                .setParameter("groupCode", groupCode)
                .executeUpdate();
        flushAndClear();

        CommonCodeInfo found = commonCodeService.findByCode(groupCode, code);

        assertThat(found).isNull();
    }

    @Test
    @DisplayName("동일 그룹에 동일 코드의 상세를 중복 추가할 수 없다")
    void addDetailRejectsDuplicateCode() {
        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupCode("GROUP_D")
                .groupName("Group D")
                .description("Group D description")
                .build();

        group.addDetail("D01", "Code D01", 1);

        assertThatThrownBy(() -> group.addDetail("D01", "Code D01 duplicated", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 공통코드");
    }

}
