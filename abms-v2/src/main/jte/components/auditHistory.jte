@import java.util.List
@import kr.co.abacus.abms.common.audit.AuditEntry
@import kr.co.abacus.abms.common.web.Ui
@param List<AuditEntry> entries
@param boolean showTarget = false
@param boolean collapsible = true
<%--
  변경 이력. 상세 화면에서는 접힌 섹션(collapsible)으로, 관리 화면에서는 펼친 목록으로 쓴다.
  showTarget: 대상(엔티티 종류·이름)을 함께 보여줄지 — 하위 엔티티 이력이 섞이는 상세 화면과 전체 목록에서 사용
--%>
!{var list = @`
    @if(entries.isEmpty())
        <p class="px-x4 py-x6 text-center t4-regular text-fg-neutral-subtle">변경 이력이 없습니다.</p>
    @else
        <ol class="divide-y divide-stroke-neutral-subtle">
            @for(AuditEntry e : entries)
                !{int shown = Math.min(e.changes().size(), 6);}
                <li class="flex gap-x3 px-x4 py-x2_5">
                    <span class="mt-1.5 size-1.5 shrink-0 rounded-full ${"DELETE".equals(e.action()) ? "bg-bg-critical-solid" : "CREATE".equals(e.action()) ? "bg-bg-positive-solid" : "bg-bg-brand-solid"}"></span>
                    <div class="min-w-0 flex-1">
                        <p class="flex flex-wrap items-baseline gap-x-x1_5 t4-regular text-fg-neutral">
                            <span class="t4-medium">${e.actor()}</span>
                            @if(showTarget)
                                <span class="text-fg-neutral-subtle">${e.entityLabel()}</span>
                                @if(e.href() != null)<a href="${e.href()}" class="link truncate">${Ui.value(e.entityName())}</a>@else<span class="truncate">${Ui.value(e.entityName())}</span>@endif
                            @endif
                            <span>${e.actionLabel()}</span>
                            <time class="ml-auto t3-regular whitespace-nowrap text-fg-neutral-subtle" datetime="${e.createdAt().toString()}" title="${Ui.dateTime(e.createdAt())}">${Ui.relative(e.createdAt())}</time>
                        </p>
                        @if(shown > 0)
                            <ul class="mt-x1 space-y-0.5 t3-regular text-fg-neutral-muted">
                                @for(AuditEntry.Change c : e.changes().subList(0, shown))
                                    <li class="break-words">
                                        <span class="text-fg-neutral-subtle">${c.label()}</span>
                                        @if(c.before() != null && c.after() != null)
                                            <del class="text-fg-neutral-subtle">${c.before()}</del> → <span class="text-fg-neutral">${c.after()}</span>
                                        @elseif(c.after() != null)
                                            <span class="text-fg-neutral">${c.after()}</span>
                                        @else
                                            <del class="text-fg-neutral-subtle">${c.before()}</del> → 비움
                                        @endif
                                    </li>
                                @endfor
                                @if(e.changes().size() > shown)<li class="text-fg-neutral-subtle">외 ${e.changes().size() - shown}개 항목</li>@endif
                            </ul>
                        @endif
                    </div>
                </li>
            @endfor
        </ol>
    @endif
`;}
@if(collapsible)
    <details class="group">
        <summary class="section-title flex cursor-pointer list-none items-center gap-x1_5 select-none [&::-webkit-details-marker]:hidden">
            <span class="transition-transform group-open:rotate-90">${kr.co.abacus.abms.common.web.Icons.of("chevron-right", "size-3.5")}</span>
            변경 이력 <span class="section-count">${entries.size()}</span>
        </summary>
        <div class="list-box">${list}</div>
    </details>
@else
    <div class="list-box">${list}</div>
@endif
