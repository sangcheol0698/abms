package kr.co.abacus.abms.adapter.infrastructure.grouppermissiongrant;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;

public interface GroupPermissionGrantRepository
        extends Repository<GroupPermissionGrant, Long>,
        kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository {

    @Override
    GroupPermissionGrant save(GroupPermissionGrant grant);

    @Override
    <S extends GroupPermissionGrant> List<S> saveAll(Iterable<S> grants);

    @Override
    List<GroupPermissionGrant> findAllByPermissionGroupIdInAndDeletedFalse(List<Long> permissionGroupIds);

}
