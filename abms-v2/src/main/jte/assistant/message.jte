@import kr.co.abacus.abms.assistant.ChatMessage
@import kr.co.abacus.abms.common.web.Icons
@param ChatMessage message
@if(message.isUser())
    <div class="flex justify-end">
        <div class="max-w-[80%] rounded-r4 bg-bg-neutral-weak px-x4 py-x2_5 t4-regular whitespace-pre-wrap text-fg-neutral">${message.getContent()}</div>
    </div>
@else
    <div class="flex gap-x3">
        <span class="mt-x0_5 flex size-6 shrink-0 items-center justify-center rounded-r1_5 bg-gradient-highlight-magic-[135deg] text-white">${Icons.of("sparkles", "size-3.5")}</span>
        <div class="prose-md min-w-0 flex-1" data-markdown>${message.getContent()}</div>
    </div>
@endif
