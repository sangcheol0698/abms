package kr.co.abacus.abms.adapter.infrastructure.permission;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.permission.Permission;

public interface PermissionRepository
        extends Repository<Permission, Long>,
        kr.co.abacus.abms.application.permission.outbound.PermissionRepository {

    @Override
    Permission save(Permission permission);

    @Override
    <S extends Permission> List<S> saveAll(Iterable<S> permissions);

    @Override
    List<Permission> findAllByIdInAndDeletedFalse(List<Long> ids);

}
