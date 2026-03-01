package kr.co.abacus.abms.domain.contract;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(String message) {
        super(message);
    }

}
