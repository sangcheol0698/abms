@import kr.co.abacus.abms.assistant.ChatMessage
@import kr.co.abacus.abms.common.web.Icons
@param ChatMessage message
@if(message.isUser())
    <div class="flex justify-end">
        <div class="max-w-[80%] rounded-2xl rounded-tr-md bg-bg-brand-solid px-4 py-2.5 text-sm whitespace-pre-wrap text-white">${message.getContent()}</div>
    </div>
@else
    <div class="flex gap-3">
        <span class="mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-lg bg-gradient-highlight-magic-[135deg] text-white">${Icons.of("sparkles", "size-3.5")}</span>
        <div class="prose-md min-w-0 max-w-[85%] rounded-2xl rounded-tl-md bg-bg-neutral-muted px-4 py-3 ring-1 ring-stroke-neutral-subtle" data-markdown>${message.getContent()}</div>
    </div>
@endif
