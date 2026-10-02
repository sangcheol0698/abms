package kr.co.abacus.abms.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 계정-권한 그룹 할당.
 */
@Entity
@Table(name = "tb_account_group_assignment")
public class AccountGroupAssignment extends BaseEntity {

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long permissionGroupId;

    protected AccountGroupAssignment() {
    }

    public static AccountGroupAssignment of(Long accountId, Long permissionGroupId) {
        AccountGroupAssignment assignment = new AccountGroupAssignment();
        assignment.accountId = accountId;
        assignment.permissionGroupId = permissionGroupId;
        return assignment;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getPermissionGroupId() {
        return permissionGroupId;
    }

}
