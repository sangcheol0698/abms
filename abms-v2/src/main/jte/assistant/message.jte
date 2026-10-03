@import kr.co.abacus.abms.assistant.ChatMessage
@import kr.co.abacus.abms.common.web.Icons
@param ChatMessage message
@if(message.isUser())
    <div class="flex justify-end">
        <div class="max-w-[80%] rounded-2xl rounded-tr-md bg-brand-600 px-4 py-2.5 text-sm whitespace-pre-wrap text-white">${message.getContent()}</div>
    </div>
@else
    <div class="flex gap-3">
        <span class="mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-violet-500 text-white">${Icons.of("sparkles", "size-3.5")}</span>
        <div class="prose-md min-w-0 max-w-[85%] rounded-2xl rounded-tl-md bg-slate-50 px-4 py-3 ring-1 ring-slate-100" data-markdown>${message.getContent()}</div>
    </div>
@endif
