@import java.util.List
@import kr.co.abacus.abms.assistant.ChatMessage
@import kr.co.abacus.abms.assistant.ChatSession
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param List<ChatSession> sessions
@param ChatSession current = null
@param List<ChatMessage> messages = null
@template.layout.app(ctx = ctx, title = current == null ? "AI 어시스턴트" : current.getTitle(), fill = true, content = @`
    <div class="flex h-[calc(100dvh-48px)] lg:h-full">
        <%-- 대화 목록 --%>
        <aside class="hidden w-64 shrink-0 flex-col border-r border-stroke-neutral-subtle @2xl:flex">
            <div class="flex items-center justify-between px-x4 pt-x4 pb-x2">
                <h1 class="t5-bold text-fg-neutral">AI 어시스턴트</h1>
                <a href="/assistant" data-shortcut="create" title="새 대화 (C)" aria-label="새 대화" class="${Seed.iconButton("neutralOutline", "xsmall")}">${Icons.of("plus", "size-4")}</a>
            </div>
            <nav class="scroll-pane flex-1 space-y-px px-x2 pb-x3" aria-label="대화 목록">
                @if(sessions.isEmpty())
                    <p class="px-x3 py-x6 text-center t3-regular text-fg-neutral-subtle">대화 기록이 없습니다.</p>
                @endif
                @for(ChatSession s : sessions)
                    <a href="/assistant/${s.id()}" class="tree-item h-8 px-x2" aria-current="${current != null && current.id().equals(s.id()) ? "page" : null}">
                        @if(s.isFavorite())${Icons.of("star", "size-3.5 shrink-0 fill-palette-yellow-400 text-palette-yellow-400")}@endif
                        <span class="flex-1 truncate">${s.getTitle()}</span>
                        <span class="shrink-0 t2-regular text-fg-neutral-subtle">${Ui.relative(s.getLastMessageAt())}</span>
                    </a>
                @endfor
            </nav>
        </aside>

        <section class="flex min-w-0 flex-1 flex-col">
            <header class="flex h-12 shrink-0 items-center justify-between gap-x3 border-b border-stroke-neutral-subtle px-x4 sm:px-x6">
                <div class="flex min-w-0 items-center gap-x2">
                    <span class="flex size-6 items-center justify-center rounded-r1_5 bg-gradient-highlight-magic-[135deg] text-white">${Icons.of("sparkles", "size-3.5")}</span>
                    <h2 class="truncate t4-bold text-fg-neutral">${current == null ? "새 대화" : current.getTitle()}</h2>
                </div>
                @if(current != null)
                    <div class="flex items-center gap-x0_5">
                        <form method="post" action="/assistant/${current.id()}/favorite">
                            <button class="${Seed.iconButton("ghost", "xsmall")}" aria-label="즐겨찾기" title="즐겨찾기">${Icons.of("star", current.isFavorite() ? "size-4 fill-palette-yellow-400 text-palette-yellow-400" : "size-4")}</button>
                        </form>
                        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" hx-post="/assistant/${current.id()}/rename" hx-prompt="새 대화 제목" aria-label="제목 변경" title="제목 변경">${Icons.of("pencil", "size-4")}</button>
                        <form method="post" action="/assistant/${current.id()}/delete" hx-confirm="이 대화를 삭제할까요?">
                            <button class="${Seed.iconButton("ghost", "xsmall")} text-fg-critical" aria-label="삭제" title="삭제">${Icons.of("trash", "size-4")}</button>
                        </form>
                    </div>
                @endif
            </header>

            @if(!ctx.aiEnabled())
                <div class="border-b border-stroke-warning-weak bg-bg-warning-weak px-x4 py-x2 t3-regular text-fg-warning-contrast sm:px-x6">
                    AI 모델이 설정되지 않았습니다. 관리자가 <code>OPENAI_API_KEY</code> 환경 변수를 설정하면 사용할 수 있습니다.
                </div>
            @endif

            <div id="messages" class="scroll-pane flex-1 space-y-6 px-x4 py-x6 sm:px-x6 [&>*]:mx-auto [&>*]:max-w-3xl" data-scroll-bottom>
                @if(current == null)
                    <div class="pt-x10">
                        <span class="flex size-10 items-center justify-center rounded-r3 bg-gradient-highlight-magic-[135deg] text-white">${Icons.of("sparkles", "size-5")}</span>
                        <h2 class="mt-x4 t7-bold text-fg-neutral">무엇을 도와드릴까요?</h2>
                        <p class="mt-x1 t4-regular text-fg-neutral-subtle">직원, 부서, 프로젝트, 월별 손익을 자연어로 물어보세요. 내 권한 범위 안의 데이터만 조회합니다.</p>
                        <div class="mt-x6 grid grid-cols-1 gap-x2 text-left @xl:grid-cols-2">
                            @for(String example : List.of("진행 중인 프로젝트 목록을 보여줘", "지난달 손익이 가장 좋은 프로젝트는?", "AI/Data사업담당 조직 구성을 알려줘", "결제 시스템 고도화 투입 인력은?"))
                                <button type="button" class="flex cursor-pointer items-center gap-x2 rounded-r3 border border-stroke-neutral-subtle px-x3 py-x2_5 text-left t4-regular text-fg-neutral-muted transition-colors hover:bg-bg-transparent-pressed hover:text-fg-neutral"
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

            <form class="px-x4 pb-x4 sm:px-x6" method="post"
                  action="${current == null ? "/assistant" : "/assistant/" + current.id() + "/messages"}"
                  hx-post="${current == null ? "/assistant" : "/assistant/" + current.id() + "/messages"}"
                  hx-target="${current == null ? null : "#messages"}" hx-swap="${current == null ? null : "beforeend"}"
                  hx-disabled-elt="find textarea, find button"
                  hx-on::before-request="abmsChat.pending(this)" hx-on::after-request="abmsChat.done(this, event)">
                <div class="mx-auto flex max-w-3xl items-end gap-x2 rounded-r4 border border-stroke-neutral-weak bg-bg-layer-default p-x2 shadow-s1 transition-shadow focus-within:border-stroke-brand-solid focus-within:shadow-[0_0_0_3px_var(--seed-color-bg-brand-weak)]">
                    <textarea id="chat-input" name="message" rows="1" required maxlength="2000" placeholder="메시지를 입력하세요 (Enter 전송, Shift+Enter 줄바꿈)"
                              class="max-h-40 min-h-9 flex-1 resize-none border-0 bg-transparent px-x2 py-x1_5 t4-regular text-fg-neutral placeholder:text-fg-placeholder focus:outline-none"
                              onkeydown="if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) { event.preventDefault(); this.form.requestSubmit(); }"
                              oninput="this.style.height = 'auto'; this.style.height = this.scrollHeight + 'px'"></textarea>
                    <button type="submit" class="${Seed.iconButton("brandSolid", "small")}" aria-label="전송" title="전송 (Enter)">${Icons.of("send", "size-4")}</button>
                </div>
                <p class="mx-auto mt-x2 max-w-3xl text-center t2-regular text-fg-neutral-subtle">AI 답변은 부정확할 수 있습니다. 중요한 수치는 원본 화면에서 확인하세요.</p>
            </form>
        </section>
    </div>
`)
