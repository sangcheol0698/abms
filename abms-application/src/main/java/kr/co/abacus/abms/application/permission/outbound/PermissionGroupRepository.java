package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

public interface PermissionGroupRepository {

    PermissionGroup save(PermissionGroup permissionGroup);

    <S extends PermissionGroup> List<S> saveAll(Iterable<S> permissionGroups);

    List<PermissionGroup> findAllByIdInAndDeletedFalse(List<Long> ids);

    List<PermissionGroup> findAllByDeletedFalse();

    Optional<PermissionGroup> findByIdAndDeletedFalse(Long id);

    Optional<PermissionGroup> findByGroupTypeAndNameAndDeletedFalse(PermissionGroupType groupType, String name);

    boolean existsByGroupTypeAndNameAndDeletedFalse(PermissionGroupType groupType, String name);

}
