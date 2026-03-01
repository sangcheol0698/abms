package kr.co.abacus.abms.domain.employee;

public class InvalidEmployeeStatusException extends RuntimeException {

    public InvalidEmployeeStatusException(String message) {
        super(message);
    }

}
