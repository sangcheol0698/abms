package kr.co.abacus.abms.application.auth;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.outbound.DefaultPermissionGroupRepository;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

@RequiredArgsConstructor
@Component
public class DefaultPermissionGroupInitializer {

    public static final String DEFAULT_PERMISSION_GROUP_NAME = "일반 그룹";
    private static final String DEFAULT_PERMISSION_GROUP_DESCRIPTION = "신규 계정에 기본 부여되는 시스템 권한 그룹이다.";

    private final DefaultPermissionGroupRepository defaultPermissionGroupRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultPermissionGroup() {
        defaultPermissionGroupRepository.findByGroupTypeAndNameAndDeletedFalse(
                PermissionGroupType.SYSTEM,
                DEFAULT_PERMISSION_GROUP_NAME
        ).orElseGet(() -> defaultPermissionGroupRepository.save(PermissionGroup.create(
                DEFAULT_PERMISSION_GROUP_NAME,
                DEFAULT_PERMISSION_GROUP_DESCRIPTION,
                PermissionGroupType.SYSTEM
        )));
    }

}
