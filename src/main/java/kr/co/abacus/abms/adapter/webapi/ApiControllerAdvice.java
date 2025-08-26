package kr.co.abacus.abms.adapter.webapi;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import kr.co.abacus.abms.domain.employee.DuplicateEmailException;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleEmailException(DuplicateEmailException exception) {
        return getProblemDetail(HttpStatus.CONFLICT, exception);
    }

    private static ProblemDetail getProblemDetail(HttpStatus httpStatus, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now(ZoneId.systemDefault()));
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }

}
