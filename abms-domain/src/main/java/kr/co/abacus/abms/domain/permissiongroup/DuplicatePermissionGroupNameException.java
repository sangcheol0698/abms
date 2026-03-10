package kr.co.abacus.abms.domain.permissiongroup;

public class DuplicatePermissionGroupNameException extends RuntimeException {

    public DuplicatePermissionGroupNameException(String message) {
        super(message);
    }

}
