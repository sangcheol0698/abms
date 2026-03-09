package kr.co.abacus.abms.application.auth.outbound;

import java.util.Optional;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

public interface DefaultPermissionGroupRepository {

    PermissionGroup save(PermissionGroup permissionGroup);

    Optional<PermissionGroup> findByGroupTypeAndNameAndDeletedFalse(PermissionGroupType groupType, String name);

}
