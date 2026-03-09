package kr.co.abacus.abms.application.auth.outbound;

import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;

public interface AccountPermissionGroupRepository {

    AccountGroupAssignment save(AccountGroupAssignment assignment);

    boolean existsByAccountIdAndPermissionGroupIdAndDeletedFalse(Long accountId, Long permissionGroupId);

}
