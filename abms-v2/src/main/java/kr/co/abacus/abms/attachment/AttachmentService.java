package kr.co.abacus.abms.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 첨부 파일. 접근 권한은 붙인 대상(프로젝트·협력사)의 조회/관리 권한을 그대로 따른다.
 */
@Service
@Transactional
public class AttachmentService {

    public static final long MAX_SIZE = 20L * 1024 * 1024;

    /** 문서·이미지·압축 파일만 허용한다. (실행 파일·HTML 등 차단) */
    static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx",
            "txt", "csv", "png", "jpg", "jpeg", "gif", "zip");

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;
    private final FileStorage fileStorage;
    private final ProjectService projectService;
    private final PartyService partyService;

    public AttachmentService(AttachmentRepository attachmentRepository, FileStorage fileStorage, ProjectService projectService,
                             PartyService partyService) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorage = fileStorage;
        this.projectService = projectService;
        this.partyService = partyService;
    }

    @Transactional(readOnly = true)
    public List<Attachment> list(LoginUser user, AttachmentOwner owner, Long ownerId) {
        checkRead(user, owner, ownerId);
        return attachmentRepository.findAllByOwnerTypeAndOwnerIdOrderByIdDesc(owner, ownerId);
    }

    /**
     * 예외로 판단하지 않는다. 트랜잭션 안에서 던진 AccessDeniedException 을 잡으면
     * 트랜잭션이 rollback-only 로 남아 화면 전체가 실패한다.
     */
    @Transactional(readOnly = true)
    public boolean canWrite(LoginUser user, AttachmentOwner owner, Long ownerId) {
        return switch (owner) {
            case PROJECT -> projectService.canWrite(user, projectService.get(ownerId));
            case PARTY -> user.has(PermissionCode.PARTY_WRITE);
        };
    }

    public Attachment upload(LoginUser user, AttachmentOwner owner, Long ownerId, @Nullable AttachmentCategory category,
                             @Nullable MultipartFile file) {
        checkWrite(user, owner, ownerId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("첨부할 파일을 선택하세요.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("파일은 20MB 이하만 첨부할 수 있습니다.");
        }
        String name = originalName(file.getOriginalFilename());
        String extension = extension(name);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("첨부할 수 없는 파일 형식입니다: ." + extension + " (문서·이미지·zip 만 가능)");
        }
        String storedPath;
        try (InputStream content = file.getInputStream()) {
            storedPath = fileStorage.store(content, extension);
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 저장하지 못했습니다.", e);
        }
        deleteFileOnRollback(storedPath);
        return attachmentRepository.save(Attachment.of(owner, ownerId, category == null ? AttachmentCategory.ETC : category, name,
                storedPath, file.getContentType(), file.getSize()));
    }

    @Transactional(readOnly = true)
    public Download download(LoginUser user, Long id) {
        Attachment attachment = get(id);
        checkRead(user, attachment.getOwnerType(), attachment.getOwnerId());
        try {
            return new Download(attachment, fileStorage.open(attachment.getStoredPath()));
        } catch (IOException e) {
            throw new NotFoundException("첨부 파일 본문을 찾을 수 없습니다: " + attachment.getOriginalName());
        }
    }

    @Transactional(readOnly = true)
    public Attachment preview(LoginUser user, Long id) {
        Attachment attachment = get(id);
        checkRead(user, attachment.getOwnerType(), attachment.getOwnerId());
        if (attachment.previewType().isEmpty()) {
            throw new BusinessException("미리 볼 수 없는 형식입니다. 내려받아 확인하세요.");
        }
        return attachment;
    }

    /** 메타데이터만 소프트 삭제한다. 파일 본문은 보관 기간 뒤 {@link AttachmentCleanupService} 가 지운다. */
    public Attachment delete(LoginUser user, Long id) {
        Attachment attachment = get(id);
        checkWrite(user, attachment.getOwnerType(), attachment.getOwnerId());
        attachment.softDelete(user.accountId());
        return attachment;
    }

    private Attachment get(Long id) {
        return attachmentRepository.findById(id).orElseThrow(() -> NotFoundException.of("첨부 파일", id));
    }

    private void checkRead(LoginUser user, AttachmentOwner owner, Long ownerId) {
        switch (owner) {
            case PROJECT -> projectService.getForRead(user, ownerId);
            case PARTY -> {
                require(user, PermissionCode.PARTY_READ);
                partyService.get(ownerId);
            }
        }
    }

    private void checkWrite(LoginUser user, AttachmentOwner owner, Long ownerId) {
        switch (owner) {
            case PROJECT -> projectService.getForWrite(user, ownerId);
            case PARTY -> {
                require(user, PermissionCode.PARTY_WRITE);
                partyService.get(ownerId);
            }
        }
    }

    private static void require(LoginUser user, PermissionCode code) {
        if (!user.has(code)) {
            throw new AccessDeniedException("첨부 파일 권한이 없습니다.");
        }
    }

    /** 경로를 떼어 낸 파일명 (브라우저에 따라 전체 경로가 오기도 한다) */
    static String originalName(@Nullable String filename) {
        String name = filename == null ? "" : filename.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).strip();
        if (name.isEmpty()) {
            throw new BusinessException("파일 이름이 없습니다.");
        }
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }

    static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void deleteFileOnRollback(String storedPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        fileStorage.delete(storedPath);
                    } catch (IOException e) {
                        log.warn("롤백된 첨부 파일 정리 실패: {}", storedPath, e);
                    }
                }
            }
        });
    }

    public record Download(Attachment attachment, InputStream content) {
    }

}
