package kr.co.abacus.abms.domain.account;

public class SamePasswordException extends RuntimeException {

    public SamePasswordException(String message) {
        super(message);
    }

}
