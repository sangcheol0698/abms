package kr.co.abacus.abms.common.domain;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String target, Object id) {
        return new NotFoundException(target + "을(를) 찾을 수 없습니다. (id=" + id + ")");
    }

}
