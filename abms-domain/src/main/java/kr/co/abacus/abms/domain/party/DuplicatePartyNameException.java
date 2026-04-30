package kr.co.abacus.abms.domain.party;

public class DuplicatePartyNameException extends RuntimeException {

    public DuplicatePartyNameException(String message) {
        super(message);
    }

}
