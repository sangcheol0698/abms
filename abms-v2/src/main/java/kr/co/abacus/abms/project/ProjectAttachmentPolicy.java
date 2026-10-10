package kr.co.abacus.abms.project;

import org.springframework.stereotype.Component;

import kr.co.abacus.abms.attachment.AttachmentOwner;
import kr.co.abacus.abms.attachment.AttachmentOwnerPolicy;
import kr.co.abacus.abms.security.LoginUser;

/** 프로젝트 첨부 파일: 프로젝트를 볼 수 있으면 읽고, 고칠 수 있으면 쓴다. */
@Component
class ProjectAttachmentPolicy implements AttachmentOwnerPolicy {

    private final ProjectService projectService;

    ProjectAttachmentPolicy(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public AttachmentOwner owner() {
        return AttachmentOwner.PROJECT;
    }

    @Override
    public void checkRead(LoginUser user, Long ownerId) {
        projectService.getForRead(user, ownerId);
    }

    @Override
    public void checkWrite(LoginUser user, Long ownerId) {
        projectService.getForWrite(user, ownerId);
    }

    @Override
    public boolean canWrite(LoginUser user, Long ownerId) {
        return projectService.canWrite(user, projectService.get(ownerId));
    }

}
