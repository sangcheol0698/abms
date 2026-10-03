@import java.util.Map
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.party.Party
@param PageView<Party> page
@param Map<Long, Long> projectCounts
@if(page.isEmpty())
    @template.components.empty(message = "등록된 협력사가 없습니다.", icon = "briefcase")
@else
    <div class="overflow-x-auto">
        <table class="table">
            <thead><tr><th>협력사</th><th>대표자</th><th>영업 담당</th><th>연락처</th><th class="num">프로젝트</th></tr></thead>
            <tbody class="divide-y divide-slate-100">
            @for(Party p : page.content())
                <tr>
                    <td><a href="/parties/${p.id()}" class="link">${p.getName()}</a></td>
                    <td>${Ui.value(p.getCeoName())}</td>
                    <td>${Ui.value(p.getSalesRepName())}</td>
                    <td class="text-xs text-slate-500">${Ui.value(p.getSalesRepPhone())}<br>${Ui.value(p.getSalesRepEmail())}</td>
                    <td class="num">${projectCounts.getOrDefault(p.id(), 0L)}</td>
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
@endif
@template.components.pagination(page = page, target = "#party-results")
