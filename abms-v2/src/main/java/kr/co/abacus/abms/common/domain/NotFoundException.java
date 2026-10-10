package kr.co.abacus.abms.common.domain;

/** 찾는 대상이 없음. 메시지 형식을 맞추려고 팩토리 메서드로만 만든다. */
public final class NotFoundException extends BusinessException {

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String target, Object id) {
        return new NotFoundException(target + "을(를) 찾을 수 없습니다. (id=" + id + ")");
    }

    /** id 로 표현할 수 없는 대상(파일 본문 등)을 찾지 못했을 때 */
    public static NotFoundException withMessage(String message) {
        return new NotFoundException(message);
    }

}
