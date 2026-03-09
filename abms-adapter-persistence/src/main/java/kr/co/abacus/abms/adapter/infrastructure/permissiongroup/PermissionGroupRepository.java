package kr.co.abacus.abms.adapter.infrastructure.permissiongroup;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;

public interface PermissionGroupRepository
        extends Repository<PermissionGroup, Long>,
        kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository,
        kr.co.abacus.abms.application.auth.outbound.DefaultPermissionGroupRepository {

    @Override
    PermissionGroup save(PermissionGroup permissionGroup);

    @Override
    <S extends PermissionGroup> List<S> saveAll(Iterable<S> permissionGroups);

    @Override
    List<PermissionGroup> findAllByIdInAndDeletedFalse(List<Long> ids);

    @Override
    java.util.Optional<PermissionGroup> findByGroupTypeAndNameAndDeletedFalse(
            kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType groupType,
            String name
    );

}
