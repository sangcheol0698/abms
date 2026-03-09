package kr.co.abacus.abms.application.permission.dto;

import java.util.List;

public record PermissionDetail(
        Long accountId,
        List<GrantedPermissionDetail> permissions
) {

}
