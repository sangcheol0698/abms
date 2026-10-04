@import kr.co.abacus.abms.project.ProjectStatus
@param ProjectStatus status
@param String size = "size-3.5"
<%-- Linear 풍 프로젝트 상태 아이콘 --%>
@if(status == ProjectStatus.IN_PROGRESS)
    <svg class="${size} shrink-0 text-fg-warning" viewBox="0 0 14 14" fill="none" aria-label="${status.label()}"><circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5"/><path d="M7 3.5a3.5 3.5 0 0 1 0 7z" fill="currentColor"/></svg>
@elseif(status == ProjectStatus.SCHEDULED)
    <svg class="${size} shrink-0 text-fg-neutral-subtle" viewBox="0 0 14 14" fill="none" aria-label="${status.label()}"><circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2.4 2"/></svg>
@elseif(status == ProjectStatus.ON_HOLD)
    <svg class="${size} shrink-0 text-fg-neutral-muted" viewBox="0 0 14 14" fill="none" aria-label="${status.label()}"><circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5"/><path d="M5.5 4.75v4.5M8.5 4.75v4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
@elseif(status == ProjectStatus.COMPLETED)
    <svg class="${size} shrink-0 text-fg-brand" viewBox="0 0 14 14" fill="none" aria-label="${status.label()}"><circle cx="7" cy="7" r="7" fill="currentColor"/><path d="m4.25 7.25 1.9 1.9 3.6-3.9" stroke="var(--seed-color-bg-layer-default)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
@else
    <svg class="${size} shrink-0 text-fg-neutral-subtle" viewBox="0 0 14 14" fill="none" aria-label="${status.label()}"><circle cx="7" cy="7" r="7" fill="currentColor"/><path d="m4.9 4.9 4.2 4.2m0-4.2-4.2 4.2" stroke="var(--seed-color-bg-layer-default)" stroke-width="1.5" stroke-linecap="round"/></svg>
@endif
