package kr.co.abacus.abms.attachment;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 첨부 파일 업로드·다운로드·삭제. 업로드·삭제 후에는 첨부 섹션(#attachments)만 다시 그린다.
 */
@Controller
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping
    public String upload(@AuthenticationPrincipal LoginUser user, @RequestParam AttachmentOwner ownerType, @RequestParam Long ownerId,
                         @RequestParam(required = false) @Nullable AttachmentCategory category,
                         @RequestParam(required = false) @Nullable MultipartFile file, Model model, HttpServletResponse response) {
        Attachment attachment = attachmentService.upload(user, ownerType, ownerId, category, file);
        Htmx.toast(response, Htmx.ToastType.SUCCESS, attachment.getOriginalName() + " 파일을 첨부했습니다.");
        return section(user, ownerType, ownerId, model);
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model, HttpServletResponse response) {
        Attachment attachment = attachmentService.delete(user, id);
        Htmx.toast(response, Htmx.ToastType.SUCCESS, attachment.getOriginalName() + " 파일을 삭제했습니다.");
        return section(user, attachment.getOwnerType(), attachment.getOwnerId(), model);
    }

    /**
     * 항상 내려받기(attachment)로 응답하고 형식은 octet-stream 으로 고정한다.
     * 업로드된 HTML·SVG 등이 브라우저에서 우리 도메인으로 실행되지 않게 하기 위해서다.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        AttachmentService.Download download = attachmentService.download(user, id);
        Attachment attachment = download.attachment();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getOriginalName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(attachment.getSize())
                .body(new InputStreamResource(download.content()));
    }

    /** 미리보기 모달: PDF 는 iframe, 이미지는 img 로 아래 inline 응답을 띄운다. */
    @GetMapping("/{id}/view")
    public String view(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        model.addAttribute("attachment", attachmentService.preview(user, id));
        return "attachment/preview";
    }

    /**
     * PDF·이미지만 inline 으로 보낸다. 형식은 확장자로 정한 값으로 고정하고 nosniff 를 붙여
     * 이름만 바꾼 HTML·SVG 가 페이지로 해석되지 않게 한다.
     */
    @GetMapping("/{id}/inline")
    public ResponseEntity<InputStreamResource> inline(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        AttachmentService.Download download = attachmentService.download(user, id);
        Attachment attachment = download.attachment();
        String type = attachment.previewType().orElseThrow(() -> new BusinessException("미리 볼 수 없는 형식입니다."));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(attachment.getOriginalName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(type))
                .contentLength(attachment.getSize())
                .body(new InputStreamResource(download.content()));
    }

    private String section(LoginUser user, AttachmentOwner ownerType, Long ownerId, Model model) {
        model.addAttribute("section", AttachmentSection.of(attachmentService, user, ownerType, ownerId));
        return "attachment/section";
    }

}
