package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;

public interface PermissionGroupRepository {

    PermissionGroup save(PermissionGroup permissionGroup);

    <S extends PermissionGroup> List<S> saveAll(Iterable<S> permissionGroups);

    List<PermissionGroup> findAllByIdInAndDeletedFalse(List<Long> ids);

}
