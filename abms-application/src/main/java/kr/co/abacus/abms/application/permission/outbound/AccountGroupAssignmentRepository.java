package kr.co.abacus.abms.application.permission.outbound;

import java.util.List;

import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;

public interface AccountGroupAssignmentRepository {

    AccountGroupAssignment save(AccountGroupAssignment assignment);

    <S extends AccountGroupAssignment> List<S> saveAll(Iterable<S> assignments);

    List<AccountGroupAssignment> findAllByAccountIdAndDeletedFalse(Long accountId);

}
