package kr.co.abacus.abms.application.permission.inbound;

import kr.co.abacus.abms.application.permission.dto.PermissionDetail;

public interface PermissionFinder {

    PermissionDetail findPermissions(Long accountId);

}
