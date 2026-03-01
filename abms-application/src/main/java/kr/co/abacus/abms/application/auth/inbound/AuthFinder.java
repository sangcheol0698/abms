package kr.co.abacus.abms.application.auth.inbound;

import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;

public interface AuthFinder {

    AuthenticatedUserInfo getCurrentUser(String username);

}
