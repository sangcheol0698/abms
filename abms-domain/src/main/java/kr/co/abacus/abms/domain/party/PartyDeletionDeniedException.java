package kr.co.abacus.abms.domain.party;

public class PartyDeletionDeniedException extends RuntimeException {

    public PartyDeletionDeniedException(String message) {
        super(message);
    }

}
