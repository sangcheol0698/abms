package kr.co.abacus.abms.application.auth.inbound;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;

public interface AuthFinder {

    AuthenticatedUserInfo getCurrentUser(String username);

    AuthenticatedUserInfo getCurrentUser(CurrentActor actor);

    Long getCurrentAccountId(String username);

}
