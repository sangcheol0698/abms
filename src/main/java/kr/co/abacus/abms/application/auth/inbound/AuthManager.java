package kr.co.abacus.abms.application.auth.inbound;

import kr.co.abacus.abms.application.auth.dto.LoginCommand;

public interface AuthManager {

    void login(LoginCommand command);

}
