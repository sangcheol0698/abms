package kr.co.abacus.abms.domain.accountgroupassignment;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_account_group_assignment",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_ACCOUNT_GROUP_ASSIGNMENT",
                columnNames = {"account_id", "permission_group_id"}
        )
)
public class AccountGroupAssignment extends AbstractEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "permission_group_id", nullable = false)
    private Long permissionGroupId;

    public static AccountGroupAssignment create(Long accountId, Long permissionGroupId) {
        AccountGroupAssignment assignment = new AccountGroupAssignment();

        assignment.accountId = requireNonNull(accountId);
        assignment.permissionGroupId = requireNonNull(permissionGroupId);

        return assignment;
    }

}
