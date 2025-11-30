package kr.co.abacus.abms.domain.party;

public class PartyNotFoundException extends RuntimeException {

    public PartyNotFoundException(String message) {
        super(message);
    }

}
