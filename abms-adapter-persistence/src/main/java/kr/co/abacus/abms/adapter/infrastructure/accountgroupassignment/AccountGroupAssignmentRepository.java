package kr.co.abacus.abms.adapter.infrastructure.accountgroupassignment;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;

public interface AccountGroupAssignmentRepository
        extends Repository<AccountGroupAssignment, Long>,
        kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository {

    @Override
    AccountGroupAssignment save(AccountGroupAssignment assignment);

    @Override
    <S extends AccountGroupAssignment> List<S> saveAll(Iterable<S> assignments);

    @Override
    List<AccountGroupAssignment> findAllByAccountIdAndDeletedFalse(Long accountId);

}
