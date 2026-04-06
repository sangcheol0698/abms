package kr.co.abacus.abms.adapter.api.weeklyreport;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import kr.co.abacus.abms.adapter.api.weeklyreport.dto.WeeklyReportDraftCreateRequest;
import kr.co.abacus.abms.adapter.api.weeklyreport.dto.WeeklyReportDraftDetailResponse;
import kr.co.abacus.abms.adapter.api.weeklyreport.dto.WeeklyReportDraftSummaryResponse;
import kr.co.abacus.abms.adapter.api.weeklyreport.dto.WeeklyReportDraftUpdateRequest;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.application.weeklyreport.inbound.WeeklyReportDraftFinder;
import kr.co.abacus.abms.application.weeklyreport.inbound.WeeklyReportDraftManager;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports/weekly/drafts")
@PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'report.read')")
public class WeeklyReportDraftApi {

    private final WeeklyReportDraftManager weeklyReportDraftManager;
    private final WeeklyReportDraftFinder weeklyReportDraftFinder;
    private final CurrentActorResolver currentActorResolver;

    @PostMapping
    public WeeklyReportDraftDetailResponse createDraft(
            @RequestBody(required = false) @Valid WeeklyReportDraftCreateRequest request,
            Authentication authentication
    ) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        WeeklyReportDraftCreateRequest safeRequest = request != null ? request : new WeeklyReportDraftCreateRequest(null, null);
        return WeeklyReportDraftDetailResponse.from(weeklyReportDraftManager.createDraft(accountId, safeRequest.toCommand()));
    }

    @GetMapping
    public List<WeeklyReportDraftSummaryResponse> getDrafts(Authentication authentication) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        return weeklyReportDraftFinder.findRecentDrafts(accountId).stream()
                .map(WeeklyReportDraftSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{draftId}")
    public WeeklyReportDraftDetailResponse getDraftDetail(
            @PathVariable Long draftId,
            Authentication authentication
    ) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        return WeeklyReportDraftDetailResponse.from(weeklyReportDraftFinder.findDraftDetail(accountId, draftId));
    }

    @PostMapping("/{draftId}/regenerate")
    public WeeklyReportDraftDetailResponse regenerateDraft(
            @PathVariable Long draftId,
            Authentication authentication
    ) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        return WeeklyReportDraftDetailResponse.from(weeklyReportDraftManager.regenerateDraft(accountId, draftId));
    }

    @PostMapping("/{draftId}/cancel")
    public WeeklyReportDraftDetailResponse cancelDraft(
            @PathVariable Long draftId,
            Authentication authentication
    ) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        return WeeklyReportDraftDetailResponse.from(weeklyReportDraftManager.cancelDraft(accountId, draftId));
    }

    @PatchMapping("/{draftId}")
    public WeeklyReportDraftDetailResponse updateDraft(
            @PathVariable Long draftId,
            @RequestBody @Valid WeeklyReportDraftUpdateRequest request,
            Authentication authentication
    ) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        return WeeklyReportDraftDetailResponse.from(weeklyReportDraftManager.updateDraft(accountId, draftId, request.toCommand()));
    }

    @DeleteMapping("/{draftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDraft(
            @PathVariable Long draftId,
            Authentication authentication
    ) {
        Long accountId = currentActorResolver.resolve(authentication).accountId();
        weeklyReportDraftManager.deleteDraft(accountId, draftId);
    }
}
