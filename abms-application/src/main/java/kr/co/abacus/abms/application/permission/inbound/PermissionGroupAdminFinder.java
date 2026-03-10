package kr.co.abacus.abms.application.permission.inbound;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.permission.dto.PermissionGroupAccountSummary;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupCatalog;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupDetail;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupSummary;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

public interface PermissionGroupAdminFinder {

    List<PermissionGroupSummary> searchGroups(@Nullable String name, @Nullable PermissionGroupType groupType);

    PermissionGroupDetail findGroupDetail(Long id);

    PermissionGroupCatalog getCatalog();

    List<PermissionGroupAccountSummary> searchAssignableAccounts(@Nullable String keyword, Long permissionGroupId);

}
