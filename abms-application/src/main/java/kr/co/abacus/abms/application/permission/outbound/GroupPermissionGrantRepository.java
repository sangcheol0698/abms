package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;

import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;

public interface GroupPermissionGrantRepository {

    GroupPermissionGrant save(GroupPermissionGrant grant);

    <S extends GroupPermissionGrant> List<S> saveAll(Iterable<S> grants);

    List<GroupPermissionGrant> findAllByPermissionGroupIdInAndDeletedFalse(List<Long> permissionGroupIds);

}
