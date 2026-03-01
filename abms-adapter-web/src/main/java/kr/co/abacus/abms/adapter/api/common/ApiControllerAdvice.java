package kr.co.abacus.abms.adapter.api.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import kr.co.abacus.abms.domain.account.AccountAlreadyExistsException;
import kr.co.abacus.abms.domain.auth.InvalidRegistrationTokenException;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.EmployeeExcelException;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.employee.InvalidEmployeeStatusException;
import kr.co.abacus.abms.domain.notification.NotificationNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectCodeDuplicateException;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler({
            DuplicateEmailException.class,
            ProjectCodeDuplicateException.class,
            InvalidEmployeeStatusException.class,
            AccountAlreadyExistsException.class,
            InvalidRegistrationTokenException.class
    })
    public ProblemDetail handleDuplicateException(Exception exception) {
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler({EmployeeNotFoundException.class, DepartmentNotFoundException.class, ProjectNotFoundException.class,
            NotificationNotFoundException.class})
    public ProblemDetail handleNotFoundException(Exception exception) {
        return getProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(EmployeeExcelException.class)
    public ProblemDetail handleExcelException(EmployeeExcelException exception) {
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException exception) {
        return getProblemDetail(HttpStatus.UNAUTHORIZED, exception);
    }

    private ProblemDetail getProblemDetail(HttpStatus httpStatus, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now(ZoneId.systemDefault()));
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        log.error("Exception occurred: {}", problemDetail, exception);

        return problemDetail;
    }

}
