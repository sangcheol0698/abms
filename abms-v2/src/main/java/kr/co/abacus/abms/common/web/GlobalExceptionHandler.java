package kr.co.abacus.abms.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;

/**
 * 컨트롤러 예외를 화면 응답으로 변환한다.
 * HTMX 요청은 토스트 이벤트(HX-Trigger)로, 일반 요청은 오류 페이지로 응답한다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final GlobalModelAdvice modelAdvice;

    public GlobalExceptionHandler(GlobalModelAdvice modelAdvice) {
        this.modelAdvice = modelAdvice;
    }

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView notFound(NotFoundException e, HttpServletRequest request, HttpServletResponse response) {
        return respond(request, response, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView noResource(HttpServletRequest request, HttpServletResponse response) {
        return respond(request, response, HttpStatus.NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.");
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView business(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
        return respond(request, response, HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ModelAndView tooLarge(HttpServletRequest request, HttpServletResponse response) {
        return respond(request, response, HttpStatus.CONTENT_TOO_LARGE, "파일은 20MB 이하만 첨부할 수 있습니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView accessDenied(AccessDeniedException e, HttpServletRequest request, HttpServletResponse response) {
        return respond(request, response, HttpStatus.FORBIDDEN, e.getMessage() == null ? "권한이 없습니다." : e.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ModelAndView integrity(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());
        return respond(request, response, HttpStatus.CONFLICT, "다른 데이터와 충돌해 저장하지 못했습니다. (중복 또는 참조 중인 데이터)");
    }

    private ModelAndView respond(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) {
        if (Htmx.isHtmx(request)) {
            Htmx.toast(response, Htmx.ToastType.ERROR, message);
            response.setHeader("HX-Reswap", "none");
            ModelAndView empty = new ModelAndView("fragments/empty");
            empty.setStatus(status);
            return empty;
        }
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", message);
        mav.addObject("ctx", modelAdvice.viewContext(request, new org.springframework.ui.ExtendedModelMap()));
        return mav;
    }

}
