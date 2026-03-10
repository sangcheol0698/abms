package kr.co.abacus.abms.adapter.infrastructure.accountgroupassignment;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;

public interface AccountGroupAssignmentRepository
        extends Repository<AccountGroupAssignment, Long>,
        kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository,
        kr.co.abacus.abms.application.auth.outbound.AccountPermissionGroupRepository {

    @Override
    AccountGroupAssignment save(AccountGroupAssignment assignment);

    @Override
    <S extends AccountGroupAssignment> List<S> saveAll(Iterable<S> assignments);

    @Override
    List<AccountGroupAssignment> findAllByAccountIdAndDeletedFalse(Long accountId);

    @Override
    List<AccountGroupAssignment> findAllByPermissionGroupId(Long permissionGroupId);

    @Override
    List<AccountGroupAssignment> findAllByPermissionGroupIdAndDeletedFalse(Long permissionGroupId);

    @Override
    List<AccountGroupAssignment> findAllByPermissionGroupIdInAndDeletedFalse(List<Long> permissionGroupIds);

    @Override
    java.util.Optional<AccountGroupAssignment> findByAccountIdAndPermissionGroupId(Long accountId, Long permissionGroupId);

    @Override
    java.util.Optional<AccountGroupAssignment> findByAccountIdAndPermissionGroupIdAndDeletedFalse(
            Long accountId,
            Long permissionGroupId
    );

    @Override
    boolean existsByAccountIdAndPermissionGroupIdAndDeletedFalse(Long accountId, Long permissionGroupId);

}
