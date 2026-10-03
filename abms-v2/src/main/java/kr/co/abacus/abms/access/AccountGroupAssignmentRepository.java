package kr.co.abacus.abms.access;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccountGroupAssignmentRepository extends JpaRepository<AccountGroupAssignment, Long> {

    List<AccountGroupAssignment> findAllByAccountId(Long accountId);

    List<AccountGroupAssignment> findAllByPermissionGroupId(Long permissionGroupId);

    Optional<AccountGroupAssignment> findByAccountIdAndPermissionGroupId(Long accountId, Long permissionGroupId);

    boolean existsByAccountIdAndPermissionGroupId(Long accountId, Long permissionGroupId);

    long countByPermissionGroupId(Long permissionGroupId);

    @Modifying
    @Query("delete from AccountGroupAssignment a where a.permissionGroupId = :groupId")
    void deleteAllByGroupId(Long groupId);

}
