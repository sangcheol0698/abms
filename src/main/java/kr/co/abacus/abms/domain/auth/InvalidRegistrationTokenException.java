package kr.co.abacus.abms.domain.auth;

public class InvalidRegistrationTokenException extends RuntimeException {

    public InvalidRegistrationTokenException(String message) {
        super(message);
    }

}
