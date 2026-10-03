@import java.util.List
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.notification.Notification
@param List<Notification> notifications
@param long unread
<div class="-m-4">
    <div class="flex items-center justify-between border-b border-stroke-neutral-subtle px-4 py-3">
        <p class="text-sm font-semibold text-fg-neutral">알림 @if(unread > 0)<span class="ml-1 text-fg-brand">${unread}</span>@endif</p>
        @if(unread > 0)
            <button type="button" class="text-xs font-medium text-fg-brand hover:underline" hx-post="/notifications/read-all" hx-target="#notification-panel"
                    hx-on::after-request="document.getElementById('notification-dot')?.remove()">모두 읽음</button>
        @endif
    </div>
    @if(notifications.isEmpty())
        <p class="px-4 py-8 text-center text-sm text-fg-neutral-subtle">알림이 없습니다.</p>
    @else
        <ul class="max-h-96 divide-y divide-stroke-neutral-subtle overflow-y-auto">
            @for(Notification n : notifications)
                <li>
                    <button type="button" class="flex w-full gap-3 px-4 py-3 text-left hover:bg-bg-neutral-muted ${n.isRead() ? "opacity-60" : ""}"
                            hx-post="/notifications/${n.id()}/read" hx-target="#notification-panel">
                        <span class="mt-1.5 size-2 shrink-0 rounded-full ${n.isRead() ? "bg-palette-gray-400" : Ui.badge(n.getType())}"></span>
                        <span class="min-w-0 flex-1">
                            <span class="block text-sm font-medium text-fg-neutral">${n.getTitle()}</span>
                            @if(n.getDescription() != null)
                                <span class="mt-0.5 block text-xs text-fg-neutral-subtle">${n.getDescription()}</span>
                            @endif
                            <span class="mt-1 block text-[11px] text-fg-placeholder">${Ui.relative(n.getCreatedAt())}</span>
                        </span>
                    </button>
                </li>
            @endfor
        </ul>
    @endif
</div>
