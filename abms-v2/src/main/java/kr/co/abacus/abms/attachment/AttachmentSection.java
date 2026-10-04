package kr.co.abacus.abms.attachment;

import java.util.List;

import kr.co.abacus.abms.security.LoginUser;

/**
 * 상세 화면의 첨부 파일 섹션 모델.
 */
public record AttachmentSection(AttachmentOwner ownerType, Long ownerId, List<Attachment> attachments, boolean canWrite) {

    public static AttachmentSection of(AttachmentService service, LoginUser user, AttachmentOwner ownerType, Long ownerId) {
        return new AttachmentSection(ownerType, ownerId, service.list(user, ownerType, ownerId), service.canWrite(user, ownerType, ownerId));
    }

    public String accept() {
        return String.join(",", AttachmentService.ALLOWED_EXTENSIONS.stream().sorted().map(e -> "." + e).toList());
    }

}
