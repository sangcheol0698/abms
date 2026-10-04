@import java.util.List
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.notification.Notification
@param List<Notification> notifications
@param long unread
<div class="-m-4">
    <div class="flex items-center justify-between border-b border-stroke-neutral-subtle px-x4 py-x2_5">
        <p class="t4-bold text-fg-neutral">알림 @if(unread > 0)<span class="ml-x1 t4-regular text-fg-neutral-subtle">${unread}</span>@endif</p>
        @if(unread > 0)
            <button type="button" class="cursor-pointer t3-medium text-fg-neutral-subtle hover:text-fg-neutral" hx-post="/notifications/read-all" hx-target="#notification-panel" data-clears-notification-dot>모두 읽음</button>
        @endif
    </div>
    @if(notifications.isEmpty())
        <p class="px-x4 py-x8 text-center t4-regular text-fg-neutral-subtle">알림이 없습니다.</p>
    @else
        <ul class="max-h-96 divide-y divide-stroke-neutral-subtle overflow-y-auto">
            @for(Notification n : notifications)
                <li>
                    <button type="button" class="flex w-full cursor-pointer gap-x3 px-x4 py-x2_5 text-left hover:bg-bg-layer-floating-pressed ${n.isRead() ? "opacity-60" : ""}"
                            hx-post="/notifications/${n.id()}/read" hx-target="#notification-panel">
                        <span class="mt-1.5 size-2 shrink-0 rounded-full ${n.isRead() ? "bg-palette-gray-400" : Ui.badge(n.getType())}"></span>
                        <span class="min-w-0 flex-1">
                            <span class="block t4-medium text-fg-neutral">${n.getTitle()}</span>
                            @if(n.getDescription() != null)
                                <span class="mt-x0_5 block t3-regular text-fg-neutral-subtle">${n.getDescription()}</span>
                            @endif
                            <span class="mt-x1 block t2-regular text-fg-neutral-subtle">${Ui.relative(n.getCreatedAt())}</span>
                        </span>
                    </button>
                </li>
            @endfor
        </ul>
    @endif
</div>
