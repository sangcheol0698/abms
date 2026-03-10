package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;

public interface AccountGroupAssignmentRepository {

    AccountGroupAssignment save(AccountGroupAssignment assignment);

    <S extends AccountGroupAssignment> List<S> saveAll(Iterable<S> assignments);

    List<AccountGroupAssignment> findAllByAccountIdAndDeletedFalse(Long accountId);

    List<AccountGroupAssignment> findAllByPermissionGroupId(Long permissionGroupId);

    List<AccountGroupAssignment> findAllByPermissionGroupIdAndDeletedFalse(Long permissionGroupId);

    List<AccountGroupAssignment> findAllByPermissionGroupIdInAndDeletedFalse(List<Long> permissionGroupIds);

    Optional<AccountGroupAssignment> findByAccountIdAndPermissionGroupId(Long accountId, Long permissionGroupId);

    Optional<AccountGroupAssignment> findByAccountIdAndPermissionGroupIdAndDeletedFalse(Long accountId, Long permissionGroupId);

}
