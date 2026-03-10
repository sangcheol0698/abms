package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.permission.Permission;

public interface PermissionRepository {

    Permission save(Permission permission);

    <S extends Permission> List<S> saveAll(Iterable<S> permissions);

    List<Permission> findAllByIdInAndDeletedFalse(List<Long> ids);

    List<Permission> findAllByCodeInAndDeletedFalse(List<String> codes);

    List<Permission> findAllByDeletedFalseOrderByCodeAsc();

    Optional<Permission> findByCodeAndDeletedFalse(String code);

}
