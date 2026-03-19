package kr.co.abacus.abms.domain.payroll;

public class PayrollNotFoundException extends RuntimeException {

    public PayrollNotFoundException(String message) {
        super(message);
    }

}
