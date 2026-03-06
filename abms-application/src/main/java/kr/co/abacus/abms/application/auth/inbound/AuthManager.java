package kr.co.abacus.abms.application.auth.inbound;

import kr.co.abacus.abms.application.auth.dto.LoginCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationConfirmCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationRequestCommand;
import kr.co.abacus.abms.application.auth.dto.ChangePasswordCommand;

public interface AuthManager {

    void requestRegistration(RegistrationRequestCommand command);

    void confirmRegistration(RegistrationConfirmCommand command);

    void login(LoginCommand command);

    void changePassword(ChangePasswordCommand command);

}
