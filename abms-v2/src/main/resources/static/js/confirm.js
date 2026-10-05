// ---------------------------------------------------------------------
// 확인창: 브라우저 기본 confirm 대신 디자인 시스템 모양의 대화상자를 띄운다.
// - hx-confirm="X 를 삭제할까요? 설명…" 은 그대로 쓰고, htmx:confirm 을 가로채 이 창으로 묻는다.
//   첫 '?' 까지가 제목, 나머지가 설명. '…할까요' 앞 동사가 확인 버튼 이름이 된다. (삭제할까요 → 삭제)
// - 삭제·비활성화·초기화·취소·제외·해제는 위험 동작: 빨간 버튼, 처음 포커스는 '닫기' (Enter 실수 방지).
// - 다른 스크립트는 window.abmsConfirm({title, description, confirmLabel, danger}) → Promise<boolean> 으로 쓴다.
// ---------------------------------------------------------------------
(function () {
    const DANGER = /삭제|비활성화|초기화|취소|제외|해제/;

    let dialog = null;

    function build() {
        dialog = document.createElement('dialog');
        dialog.className = 'confirm-dialog';
        dialog.setAttribute('aria-labelledby', 'confirm-title');
        dialog.setAttribute('aria-describedby', 'confirm-description');
        dialog.innerHTML = `
            <div class="flex gap-x3 px-x5 pt-x5">
                <span class="confirm-icon" aria-hidden="true"></span>
                <div class="min-w-0 flex-1">
                    <h2 id="confirm-title" class="t5-bold text-fg-neutral"></h2>
                    <p id="confirm-description" class="mt-x1_5 t4-regular whitespace-pre-line text-fg-neutral-muted"></p>
                </div>
            </div>
            <form method="dialog" class="flex justify-end gap-x2 px-x5 pt-x5 pb-x4">
                <button value="cancel" class="seed-action-button seed-action-button--variant_neutralWeak seed-action-button--size_medium seed-action-button--layout_withText seed-action-button--size_medium-layout_withText" data-confirm-cancel>닫기</button>
                <button value="ok" class="seed-action-button seed-action-button--size_medium seed-action-button--layout_withText seed-action-button--size_medium-layout_withText" data-confirm-ok></button>
            </form>`;
        // 바깥(배경)을 누르면 닫기
        dialog.addEventListener('click', (e) => {
            if (e.target === dialog) dialog.close('cancel');
        });
        document.body.append(dialog);
    }

    const ICONS = {
        danger: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="size-5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z"/></svg>',
        normal: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="size-5"><path stroke-linecap="round" stroke-linejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 5.25h.008v.008H12v-.008Z"/></svg>'
    };

    /** @returns {Promise<boolean>} */
    function abmsConfirm({title, description = '', confirmLabel = '확인', danger = false}) {
        if (!dialog) build();
        if (dialog.open) dialog.close('cancel');
        dialog.querySelector('#confirm-title').textContent = title;
        const desc = dialog.querySelector('#confirm-description');
        desc.textContent = description;
        desc.hidden = !description;
        const icon = dialog.querySelector('.confirm-icon');
        icon.innerHTML = danger ? ICONS.danger : ICONS.normal;
        icon.classList.toggle('is-danger', danger);
        const ok = dialog.querySelector('[data-confirm-ok]');
        ok.textContent = confirmLabel;
        ok.classList.toggle('seed-action-button--variant_criticalSolid', danger);
        ok.classList.toggle('seed-action-button--variant_brandSolid', !danger);
        dialog.returnValue = '';
        // 확인창을 띄운 메뉴(더보기 등)는 닫는다.
        document.querySelectorAll('details[data-dropdown][open]').forEach((d) => d.removeAttribute('open'));
        const opener = document.activeElement;
        return new Promise((resolve) => {
            dialog.addEventListener('close', () => {
                resolve(dialog.returnValue === 'ok');
                if (opener && opener.isConnected) opener.focus({preventScroll: true});
            }, {once: true});
            dialog.showModal();
            (danger ? dialog.querySelector('[data-confirm-cancel]') : ok).focus();
        });
    }

    /** "X 를 삭제할까요? 삭제 후에도 복구할 수 있습니다." → 제목·설명·버튼 이름·위험 여부 */
    function parse(question) {
        const cut = question.indexOf('?');
        const title = cut < 0 ? question : question.slice(0, cut + 1);
        const description = cut < 0 ? '' : question.slice(cut + 1).trim();
        const verb = title.match(/([가-힣]+)(\s처리)?할까요/);
        return {title, description, confirmLabel: verb ? verb[1] + (verb[2] || '') : '확인', danger: DANGER.test(title)};
    }

    window.abmsConfirm = abmsConfirm;
    window.abmsConfirm.parse = parse;

    document.body.addEventListener('htmx:confirm', (e) => {
        const question = e.detail.question;
        if (!question) return;
        e.preventDefault();
        const elt = e.detail.elt;
        const options = parse(question);
        if (elt.dataset.confirmLabel) options.confirmLabel = elt.dataset.confirmLabel;
        abmsConfirm(options).then((yes) => {
            if (yes) e.detail.issueRequest(true);
        });
    });
})();
