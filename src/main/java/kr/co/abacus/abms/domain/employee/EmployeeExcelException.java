package kr.co.abacus.abms.domain.employee;

public class EmployeeExcelException extends RuntimeException {

    public EmployeeExcelException(String message) {
        super(message);
    }

    public EmployeeExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
