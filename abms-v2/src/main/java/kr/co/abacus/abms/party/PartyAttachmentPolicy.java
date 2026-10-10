package kr.co.abacus.abms.party;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.attachment.AttachmentOwner;
import kr.co.abacus.abms.attachment.AttachmentOwnerPolicy;
import kr.co.abacus.abms.security.LoginUser;

/** 협력사 첨부 파일: 협력사 조회·관리 권한으로 판단한다. */
@Component
class PartyAttachmentPolicy implements AttachmentOwnerPolicy {

    private final PartyService partyService;

    PartyAttachmentPolicy(PartyService partyService) {
        this.partyService = partyService;
    }

    @Override
    public AttachmentOwner owner() {
        return AttachmentOwner.PARTY;
    }

    @Override
    public void checkRead(LoginUser user, Long ownerId) {
        require(user, PermissionCode.PARTY_READ);
        partyService.get(ownerId);
    }

    @Override
    public void checkWrite(LoginUser user, Long ownerId) {
        require(user, PermissionCode.PARTY_WRITE);
        partyService.get(ownerId);
    }

    @Override
    public boolean canWrite(LoginUser user, Long ownerId) {
        return user.has(PermissionCode.PARTY_WRITE);
    }

    private static void require(LoginUser user, PermissionCode code) {
        if (!user.has(code)) {
            throw new AccessDeniedException("첨부 파일 권한이 없습니다.");
        }
    }

}
