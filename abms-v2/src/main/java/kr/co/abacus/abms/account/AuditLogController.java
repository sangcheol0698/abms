package kr.co.abacus.abms.account;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.audit.AuditQueryService;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.PageView;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 전체 변경 이력 (관리). 계정 관리 권한이 있는 사용자만 본다.
 */
@Controller
@RequestMapping("/admin/audit-logs")
public class AuditLogController {

    private static final int PAGE_SIZE = 50;

    private final AuditQueryService auditQueryService;

    public AuditLogController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal LoginUser user, @RequestParam(required = false) @Nullable String type,
                       @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model) {
        if (!user.has(PermissionCode.ACCOUNT_MANAGE)) {
            throw new AccessDeniedException("변경 이력 조회 권한이 없습니다.");
        }
        String entityType = type == null || type.isBlank() ? null : type;
        int current = Math.max(page, 0);
        var entries = auditQueryService.recent(entityType, current * PAGE_SIZE, PAGE_SIZE);
        var result = new PageImpl<>(entries, PageRequest.of(current, PAGE_SIZE), auditQueryService.count(entityType));
        String baseUrl = UriComponentsBuilder.fromPath("/admin/audit-logs").query(request.getQueryString()).build().toUriString();
        model.addAttribute("page", PageView.of(result, baseUrl));
        model.addAttribute("types", auditQueryService.entityTypes());
        model.addAttribute("type", entityType);
        if (Htmx.targets(request, "audit-results")) {
            return "admin/auditLogResults";
        }
        return "admin/auditLogs";
    }

}
