package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;

import kr.co.abacus.abms.domain.permission.Permission;

public interface PermissionRepository {

    Permission save(Permission permission);

    <S extends Permission> List<S> saveAll(Iterable<S> permissions);

    List<Permission> findAllByIdInAndDeletedFalse(List<Long> ids);

}
