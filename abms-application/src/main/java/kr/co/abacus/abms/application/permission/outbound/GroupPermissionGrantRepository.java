package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;

public interface GroupPermissionGrantRepository {

    GroupPermissionGrant save(GroupPermissionGrant grant);

    <S extends GroupPermissionGrant> List<S> saveAll(Iterable<S> grants);

    List<GroupPermissionGrant> findAllByPermissionGroupId(Long permissionGroupId);

    List<GroupPermissionGrant> findAllByPermissionGroupIdAndDeletedFalse(Long permissionGroupId);

    List<GroupPermissionGrant> findAllByPermissionGroupIdInAndDeletedFalse(List<Long> permissionGroupIds);

    Optional<GroupPermissionGrant> findByPermissionGroupIdAndPermissionIdAndScope(
            Long permissionGroupId,
            Long permissionId,
            kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope scope
    );

    Optional<GroupPermissionGrant> findByPermissionGroupIdAndPermissionIdAndScopeAndDeletedFalse(
            Long permissionGroupId,
            Long permissionId,
            kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope scope
    );

}
