package kr.co.abacus.abms.attachment;

import kr.co.abacus.abms.security.LoginUser;

/**
 * 첨부 대상(프로젝트, 협력사)별 읽기·쓰기 권한 확인. 대상 기능이 구현해
 * {@link AttachmentService} 가 프로젝트·협력사 패키지를 직접 알지 않게 한다.
 */
public interface AttachmentOwnerPolicy {

    AttachmentOwner owner();

    /** 대상을 볼 수 없거나 없으면 예외를 던진다. */
    void checkRead(LoginUser user, Long ownerId);

    /** 대상을 고칠 수 없거나 없으면 예외를 던진다. */
    void checkWrite(LoginUser user, Long ownerId);

    /**
     * 예외로 판단하지 않는다. 트랜잭션 안에서 던진 AccessDeniedException 을 잡으면
     * 트랜잭션이 rollback-only 로 남아 화면 전체가 실패한다.
     */
    boolean canWrite(LoginUser user, Long ownerId);

}
