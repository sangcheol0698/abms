@import java.util.List
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.notification.Notification
@param List<Notification> notifications
@param long unread
<div class="-m-4">
    <div class="flex items-center justify-between border-b border-slate-100 px-4 py-3">
        <p class="text-sm font-semibold text-slate-900">알림 @if(unread > 0)<span class="ml-1 text-brand-600">${unread}</span>@endif</p>
        @if(unread > 0)
            <button type="button" class="text-xs font-medium text-brand-600 hover:underline" hx-post="/notifications/read-all" hx-target="#notification-panel"
                    hx-on::after-request="document.getElementById('notification-dot')?.remove()">모두 읽음</button>
        @endif
    </div>
    @if(notifications.isEmpty())
        <p class="px-4 py-8 text-center text-sm text-slate-500">알림이 없습니다.</p>
    @else
        <ul class="max-h-96 divide-y divide-slate-100 overflow-y-auto">
            @for(Notification n : notifications)
                <li>
                    <button type="button" class="flex w-full gap-3 px-4 py-3 text-left hover:bg-slate-50 ${n.isRead() ? "opacity-60" : ""}"
                            hx-post="/notifications/${n.id()}/read" hx-target="#notification-panel">
                        <span class="mt-1.5 size-2 shrink-0 rounded-full ${n.isRead() ? "bg-slate-300" : Ui.badge(n.getType())}"></span>
                        <span class="min-w-0 flex-1">
                            <span class="block text-sm font-medium text-slate-900">${n.getTitle()}</span>
                            @if(n.getDescription() != null)
                                <span class="mt-0.5 block text-xs text-slate-500">${n.getDescription()}</span>
                            @endif
                            <span class="mt-1 block text-[11px] text-slate-400">${Ui.relative(n.getCreatedAt())}</span>
                        </span>
                    </button>
                </li>
            @endfor
        </ul>
    @endif
</div>
