package kr.co.abacus.abms.common.domain;

/**
 * 업무 규칙 위반을 나타내는 예외. 메시지는 사용자에게 그대로 노출된다.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

}
