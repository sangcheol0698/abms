@import java.util.List
@import kr.co.abacus.abms.assistant.ChatMessage
@import kr.co.abacus.abms.assistant.ChatSession
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@param ViewContext ctx
@param List<ChatSession> sessions
@param ChatSession current = null
@param List<ChatMessage> messages = null
@template.layout.app(ctx = ctx, title = current == null ? "AI 어시스턴트" : current.getTitle(), content = @`
    <div class="-my-6 flex h-[calc(100vh-4rem)] gap-4 py-4 lg:-mx-2">
        <aside class="card hidden w-72 shrink-0 flex-col overflow-hidden md:flex">
            <div class="border-b border-slate-100 p-3">
                <a href="/assistant" class="btn btn-primary w-full">${Icons.of("plus", "size-4")} 새 대화</a>
            </div>
            <nav class="flex-1 overflow-y-auto p-2" aria-label="대화 목록">
                @if(sessions.isEmpty())
                    <p class="px-3 py-6 text-center text-xs text-slate-400">대화 기록이 없습니다.</p>
                @endif
                @for(ChatSession s : sessions)
                    <a href="/assistant/${s.id()}" class="group flex items-center gap-2 rounded-lg px-3 py-2 text-sm ${current != null && current.id().equals(s.id()) ? "bg-brand-50 text-brand-700" : "text-slate-700 hover:bg-slate-50"}">
                        @if(s.isFavorite())${Icons.of("star", "size-3.5 shrink-0 fill-amber-400 text-amber-400")}@endif
                        <span class="flex-1 truncate">${s.getTitle()}</span>
                        <span class="shrink-0 text-[11px] text-slate-400">${Ui.relative(s.getLastMessageAt())}</span>
                    </a>
                @endfor
            </nav>
        </aside>

        <section class="card flex min-w-0 flex-1 flex-col overflow-hidden">
            <header class="flex items-center justify-between gap-3 border-b border-slate-100 px-5 py-3">
                <div class="flex min-w-0 items-center gap-2">
                    <span class="flex size-8 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-violet-500 text-white">${Icons.of("sparkles", "size-4")}</span>
                    <h1 class="truncate text-sm font-semibold text-slate-900">${current == null ? "AI 어시스턴트" : current.getTitle()}</h1>
                </div>
                @if(current != null)
                    <div class="flex items-center gap-1">
                        <form method="post" action="/assistant/${current.id()}/favorite">
                            <button class="btn btn-ghost btn-sm" aria-label="즐겨찾기">${Icons.of("star", current.isFavorite() ? "size-4 fill-amber-400 text-amber-400" : "size-4")}</button>
                        </form>
                        <button type="button" class="btn btn-ghost btn-sm" hx-post="/assistant/${current.id()}/rename" hx-prompt="새 대화 제목" aria-label="제목 변경">${Icons.of("pencil", "size-4")}</button>
                        <form method="post" action="/assistant/${current.id()}/delete" hx-confirm="이 대화를 삭제할까요?">
                            <button class="btn btn-danger-ghost btn-sm" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
                        </form>
                    </div>
                @endif
            </header>

            @if(!ctx.aiEnabled())
                <div class="border-b border-amber-200 bg-amber-50 px-5 py-2.5 text-xs text-amber-800">
                    AI 모델이 설정되지 않았습니다. 관리자가 <code>OPENAI_API_KEY</code> 환경 변수를 설정하면 사용할 수 있습니다.
                </div>
            @endif

            <div id="messages" class="flex-1 space-y-5 overflow-y-auto px-5 py-6" data-scroll-bottom>
                @if(current == null)
                    <div class="mx-auto max-w-xl pt-10 text-center">
                        <span class="mx-auto flex size-12 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-violet-500 text-white shadow-lg shadow-brand-500/30">${Icons.of("sparkles", "size-6")}</span>
                        <h2 class="mt-4 text-lg font-semibold text-slate-900">무엇을 도와드릴까요?</h2>
                        <p class="mt-1 text-sm text-slate-500">직원, 부서, 프로젝트, 월별 손익을 자연어로 물어보세요. 내 권한 범위 안의 데이터만 조회합니다.</p>
                        <div class="mt-6 grid grid-cols-1 gap-2 text-left sm:grid-cols-2">
                            @for(String example : List.of("진행 중인 프로젝트 목록을 보여줘", "지난달 손익이 가장 좋은 프로젝트는?", "AI/Data사업담당 조직 구성을 알려줘", "결제 시스템 고도화 투입 인력은?"))
                                <button type="button" class="rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-700 ring-1 ring-slate-200 hover:bg-white hover:ring-brand-300"
                                        onclick="const t = document.getElementById('chat-input'); t.value = this.textContent.trim(); t.focus();">${example}</button>
                            @endfor
                        </div>
                    </div>
                @else
                    @for(ChatMessage m : messages)
                        @template.assistant.message(message = m)
                    @endfor
                @endif
            </div>

            <form class="border-t border-slate-100 p-4" method="post"
                  action="${current == null ? "/assistant" : "/assistant/" + current.id() + "/messages"}"
                  hx-post="${current == null ? "/assistant" : "/assistant/" + current.id() + "/messages"}"
                  hx-target="${current == null ? null : "#messages"}" hx-swap="${current == null ? null : "beforeend"}"
                  hx-disabled-elt="find textarea, find button"
                  hx-on::before-request="abmsChat.pending(this)" hx-on::after-request="abmsChat.done(this, event)">
                <div class="flex items-end gap-2 rounded-xl bg-white p-2 ring-1 ring-slate-300 focus-within:ring-2 focus-within:ring-brand-500">
                    <textarea id="chat-input" name="message" rows="1" required maxlength="2000" placeholder="메시지를 입력하세요 (Enter 전송, Shift+Enter 줄바꿈)"
                              class="max-h-40 min-h-9 flex-1 resize-none border-0 bg-transparent px-2 py-1.5 text-sm focus:outline-none"
                              onkeydown="if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) { event.preventDefault(); this.form.requestSubmit(); }"
                              oninput="this.style.height = 'auto'; this.style.height = this.scrollHeight + 'px'"></textarea>
                    <button type="submit" class="btn btn-primary !p-2" aria-label="전송">${Icons.of("send", "size-4")}</button>
                </div>
                <p class="mt-2 text-center text-[11px] text-slate-400">AI 답변은 부정확할 수 있습니다. 중요한 수치는 원본 화면에서 확인하세요.</p>
            </form>
        </section>
    </div>
`)
