package kr.co.abacus.abms.application.auth;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("기본 권한 그룹 초기화")
class DefaultPermissionGroupInitializerTest extends IntegrationTestBase {

    @Autowired
    private DefaultPermissionGroupInitializer defaultPermissionGroupInitializer;

    @Test
    @DisplayName("애플리케이션 시작 시 기본 시스템 권한 그룹을 생성한다")
    void should_initializeDefaultPermissionGroup_onApplicationReady() {
        List<PermissionGroup> defaultPermissionGroups = findDefaultPermissionGroups();

        assertThat(defaultPermissionGroups).hasSize(1);
        assertThat(defaultPermissionGroups.getFirst().getGroupType()).isEqualTo(PermissionGroupType.SYSTEM);
        assertThat(defaultPermissionGroups.getFirst().getDescription()).isNotBlank();
    }

    @Test
    @DisplayName("기본 권한 그룹 초기화는 중복 생성 없이 한 번만 수행된다")
    void should_notCreateDuplicateDefaultPermissionGroup_whenInitializerRunsAgain() {
        defaultPermissionGroupInitializer.initializeDefaultPermissionGroup();
        flushAndClear();

        assertThat(findDefaultPermissionGroups()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    private List<PermissionGroup> findDefaultPermissionGroups() {
        return entityManager.createQuery("""
                        select permissionGroup
                        from PermissionGroup permissionGroup
                        where permissionGroup.groupType = :groupType
                          and permissionGroup.name = :name
                          and permissionGroup.deleted = false
                        """)
                .setParameter("groupType", PermissionGroupType.SYSTEM)
                .setParameter("name", DefaultPermissionGroupInitializer.DEFAULT_PERMISSION_GROUP_NAME)
                .getResultList();
    }

}
