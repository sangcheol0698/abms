package kr.co.abacus.abms.application.auth.outbound;

public interface CredentialAuthenticator {

    void authenticate(String username, String password);

}
