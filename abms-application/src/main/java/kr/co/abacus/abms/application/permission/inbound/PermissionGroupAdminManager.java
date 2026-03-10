package kr.co.abacus.abms.application.permission.inbound;

import kr.co.abacus.abms.application.permission.dto.PermissionGroupUpsertCommand;

public interface PermissionGroupAdminManager {

    Long createGroup(PermissionGroupUpsertCommand command);

    void updateGroup(Long id, PermissionGroupUpsertCommand command);

    void deleteGroup(Long id, Long actorAccountId);

    void assignAccount(Long permissionGroupId, Long accountId);

    void unassignAccount(Long permissionGroupId, Long accountId, Long actorAccountId);

}
