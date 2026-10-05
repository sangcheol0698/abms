@import java.util.Map
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.party.Party
@param PageView<Party> page
@param Map<Long, Long> projectCounts
@param Map<Long, kr.co.abacus.abms.party.PartyContact> primaryContacts
<div class="min-h-0 flex-1 lg:overflow-y-auto lg:overscroll-contain" data-fill-scroll>
@if(page.isEmpty())
    @template.components.empty(message = "등록된 협력사가 없습니다.", icon = "briefcase")
@else
    @for(Party p : page.content())
        !{long count = projectCounts.getOrDefault(p.id(), 0L);}
        !{var contact = primaryContacts.get(p.id());}
        <a href="/parties/${p.id()}" class="list-row sm:px-x6 lg:px-x8">
            <span class="flex size-6 shrink-0 items-center justify-center rounded-r1_5 bg-bg-neutral-weak t3-bold text-fg-neutral-muted">${p.getName().substring(0, 1)}</span>
            <span class="flex min-w-0 flex-1 items-center gap-x2"><span class="truncate t4-medium">${p.getName()}</span><span class="${kr.co.abacus.abms.common.web.Seed.badge(p.getPartyType() == kr.co.abacus.abms.party.PartyType.PARTNER ? "neutral" : "informative", p.getPartyType() == kr.co.abacus.abms.party.PartyType.BOTH ? "outline" : "weak")}">${p.getPartyType().label()}</span></span>
            <span class="list-cell-muted hidden w-28 @2xl:block">${p.getIndustry() == null ? "-" : p.getIndustry()}</span>
            <span class="list-cell-muted hidden w-28 @2xl:block">${p.getCeoName() == null ? "-" : "대표 " + p.getCeoName()}</span>
            <span class="list-cell-muted hidden w-32 @3xl:block">${contact == null ? "-" : contact.getName()}</span>
            <span class="list-cell-muted hidden w-56 @5xl:block">${contact == null ? "" : Ui.value(contact.getEmail())}</span>
            <span class="w-24 shrink-0 text-right t3-regular ${count > 0 ? "text-fg-neutral" : "text-fg-neutral-subtle"}">프로젝트 ${count}건</span>
        </a>
    @endfor
@endif
</div>
<div class="shrink-0 bg-bg-layer-default sm:px-x2 lg:px-x4">@template.components.pagination(page = page, target = "#party-results")</div>
