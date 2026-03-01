package kr.co.abacus.abms.application.commoncode;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.commoncode.dto.CommonCodeInfo;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetailId;
import kr.co.abacus.abms.domain.commoncode.CommonCodeGroup;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("공통코드 서비스 통합 테스트")
class CommonCodeServiceTest extends IntegrationTestBase {

    @Autowired
    private CommonCodeService commonCodeService;

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
        CommonCodeDetail detail = CommonCodeDetail.builder()
                .id(new CommonCodeDetailId(groupCode, code))
                .name("Code A01")
                .sortOrder(1)
                .build();
        group.addDetail(detail);

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
        CommonCodeDetail detail = CommonCodeDetail.builder()
                .id(new CommonCodeDetailId(groupCode, code))
                .name("Code B01")
                .sortOrder(1)
                .build();
        group.addDetail(detail);

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
        CommonCodeDetail detail = CommonCodeDetail.builder()
                .id(new CommonCodeDetailId(groupCode, code))
                .name("Code C01")
                .sortOrder(1)
                .build();
        group.addDetail(detail);

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

}
