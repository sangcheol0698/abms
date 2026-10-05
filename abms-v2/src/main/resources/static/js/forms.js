// ---------------------------------------------------------------------
// 폼 컨트롤 보강. 서버로 가는 값과 이름은 그대로 두고 화면만 바꾼다.
// - 날짜·월·일시(input[type=date|month|datetime-local]): 직접 입력 + 달력 팝오버.
//   브라우저 기본 달력은 모양이 제각각이고 type=month 는 Firefox·Safari 에서 달력이 없다.
//   보이는 칸은 이름 없는 텍스트 입력, 실제 값(ISO)은 옆의 hidden 입력이 보낸다.
// - 검색 선택(select[data-combobox]): 입력해서 고르는 목록. 초성(ㅎㄱㄷ)으로도 찾는다.
// - 파일 놓기([data-file-drop]): 끌어다 놓거나 눌러서 고르고, 고른 파일 이름·크기를 보여준다.
// - 형식 입력(마스크): 숫자만 받고 구분자는 자동으로 넣는다.
//   날짜(2026-03-05), 전화번호(010-1234-5678 · 02-123-4567 · 1588-1234), 사업자등록번호(000-00-00000),
//   우편번호(5자리), 금액(1,000,000 — 서버에는 숫자만 보낸다).
// - 숫자 입력: 포커스 중 마우스 휠로 값이 바뀌지 않게 한다.
// ---------------------------------------------------------------------
(function () {
    const pad = (n) => String(n).padStart(2, '0');
    const isoDate = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
    const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

    function el(tag, className, text) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text;
        return node;
    }

    // ---- 떠 있는 패널: 모달 안이면 dialog 에, 아니면 body 에 붙여 잘리지 않게 고정 위치로 띄운다 ----
    let floating = null;

    function openFloating(anchor, panel, onClose, onEscape) {
        closeFloating();
        const host = anchor.closest('dialog') || document.body;
        panel.classList.add('popover');
        host.append(panel);
        floating = {anchor, panel, onClose, onEscape};
        placeFloating();
    }

    function placeFloating() {
        if (!floating) return;
        const {anchor, panel} = floating;
        const rect = anchor.getBoundingClientRect();
        const gap = 4;
        const below = window.innerHeight - rect.bottom;
        panel.style.minWidth = rect.width + 'px';
        const height = panel.offsetHeight;
        const top = below < height + gap && rect.top > below ? rect.top - height - gap : rect.bottom + gap;
        const left = Math.max(8, Math.min(rect.left, window.innerWidth - panel.offsetWidth - 8));
        panel.style.top = Math.max(8, top) + 'px';
        panel.style.left = left + 'px';
    }

    function closeFloating() {
        if (!floating) return;
        const {panel, onClose} = floating;
        floating = null;
        panel.remove();
        if (onClose) onClose();
    }

    // Esc 는 어디에 포커스가 있든 떠 있는 패널만 닫는다. (모달까지 닫히지 않게 캡처 단계에서 가로챈다)
    document.addEventListener('keydown', (e) => {
        if (!floating || e.key !== 'Escape' || e.isComposing) return;
        e.preventDefault();
        e.stopPropagation();
        const {anchor, onEscape} = floating;
        closeFloating();
        if (onEscape) onEscape();
        anchor.querySelector('input:not([type=hidden])')?.focus({preventScroll: true});
    }, true);

    document.addEventListener('pointerdown', (e) => {
        if (floating && !floating.panel.contains(e.target) && !floating.anchor.contains(e.target)) closeFloating();
    });
    // 스크롤·크기 변경(모바일 키보드 포함)에는 닫지 않고 위치만 다시 잡는다.
    document.addEventListener('scroll', (e) => {
        if (floating && !floating.panel.contains(e.target)) placeFloating();
    }, true);
    window.addEventListener('resize', () => placeFloating());
    document.body.addEventListener('htmx:beforeSwap', () => closeFloating());
    document.addEventListener('close', () => closeFloating(), true); // 모달이 닫힐 때
    document.body.addEventListener('htmx:beforeHistorySave', () => closeFloating());

    // ---------------------------------------------------------------------
    // 형식 입력(마스크)
    // ---------------------------------------------------------------------
    const digitsOf = (text) => text.replace(/\D/g, '');

    /** 숫자 외 입력은 막고, 입력할 때마다 format(숫자) 로 다시 그린다. 커서는 같은 숫자 뒤에 둔다. */
    function mask(input, format, validate) {
        input.inputMode = 'numeric';
        input.autocomplete = 'off';

        function reformat() {
            const value = input.value;
            const atEnd = input.selectionStart === null || input.selectionStart >= value.length;
            const before = digitsOf(value.slice(0, input.selectionStart ?? value.length)).length;
            const formatted = format(digitsOf(value));
            if (formatted !== value) {
                input.value = formatted;
                if (document.activeElement === input) {
                    let pos = formatted.length;
                    if (!atEnd) {
                        let seen = 0;
                        pos = 0;
                        while (pos < formatted.length && seen < before) {
                            if (/\d/.test(formatted[pos])) seen++;
                            pos++;
                        }
                    }
                    input.setSelectionRange(pos, pos);
                }
            }
            if (validate) input.setCustomValidity(input.value ? validate(digitsOf(input.value)) : '');
        }

        input.addEventListener('beforeinput', (e) => {
            if (e.inputType === 'insertText' && e.data && /\D/.test(e.data)) {
                e.preventDefault();
                // 구분자(- . / : 공백 등)는 자동으로 들어가므로 조용히 무시하고, 문자만 알린다.
                if (/^[-./: ()+,]$/.test(e.data)) return;
                input.classList.remove('is-rejected');
                void input.offsetWidth;
                input.classList.add('is-rejected');
            }
        });
        input.addEventListener('animationend', () => input.classList.remove('is-rejected'));
        input.addEventListener('input', reformat);
        // 구분자 바로 뒤에서 지우면 구분자 대신 그 앞 숫자를 지운다. (구분자는 다시 생기므로)
        input.addEventListener('keydown', (e) => {
            const {selectionStart: start, selectionEnd: end, value} = input;
            if (start === null || start !== end) return;
            if (e.key === 'Backspace' && start > 0 && /\D/.test(value[start - 1])) input.setSelectionRange(start - 1, start - 1);
            if (e.key === 'Delete' && start < value.length && /\D/.test(value[start])) input.setSelectionRange(start + 1, start + 1);
        });
        reformat();
    }

    /** 월·일·시·분 앞자리가 클 수 없는 숫자면 0 을 붙인다. (3 → 03월) */
    function dateDigits(digits, parts) {
        const out = [digits.slice(0, 4)];
        let rest = digits.slice(4);
        for (const firstMax of parts) {
            if (!rest) break;
            if (+rest[0] > firstMax) {
                out.push('0' + rest[0]);
                rest = rest.slice(1);
            } else {
                out.push(rest.slice(0, 2));
                rest = rest.slice(2);
            }
        }
        return out;
    }

    const FORMATS = {
        date: (d) => dateDigits(d, [1, 3]).join('-'),
        month: (d) => dateDigits(d, [1]).join('-'),
        'datetime-local': (d) => {
            const [y, mo, day, h, mi] = dateDigits(d, [1, 3, 2, 5]);
            return [y, mo, day].filter((x) => x !== undefined).join('-') + (h !== undefined ? ' ' + h : '') + (mi !== undefined ? ':' + mi : '');
        },
        phone: (d) => {
            if (d.startsWith('02')) {
                d = d.slice(0, 10);
                if (d.length <= 2) return d;
                if (d.length <= 5) return `02-${d.slice(2)}`;
                return d.length <= 9 ? `02-${d.slice(2, 5)}-${d.slice(5)}` : `02-${d.slice(2, 6)}-${d.slice(6)}`;
            }
            if (/^1[5-9]/.test(d)) {
                d = d.slice(0, 8);
                return d.length <= 4 ? d : `${d.slice(0, 4)}-${d.slice(4)}`;
            }
            d = d.slice(0, 11);
            if (d.length <= 3) return d;
            if (d.length <= 6) return `${d.slice(0, 3)}-${d.slice(3)}`;
            return d.length <= 10 ? `${d.slice(0, 3)}-${d.slice(3, 6)}-${d.slice(6)}` : `${d.slice(0, 3)}-${d.slice(3, 7)}-${d.slice(7)}`;
        },
        business: (d) => {
            d = d.slice(0, 10);
            return [d.slice(0, 3), d.slice(3, 5), d.slice(5)].filter(Boolean).join('-');
        },
        zip: (d) => d.slice(0, 5),
        money: (d) => d.replace(/^0+(?=\d)/, '').slice(0, 15).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    };

    const VALIDATORS = {
        phone: (d) => {
            const ok = d.startsWith('02') ? d.length >= 9 : /^1[5-9]/.test(d) ? d.length === 8 : d.length >= 10;
            return ok ? '' : '전화번호를 끝까지 입력하세요.';
        },
        business: (d) => (d.length === 10 ? '' : '사업자등록번호 10자리를 입력하세요.')
    };

    /** 금액: 보이는 칸은 쉼표, 실제 값(숫자)은 hidden 이 보낸다. */
    function moneyField(input) {
        const hidden = el('input');
        hidden.type = 'hidden';
        hidden.name = input.name;
        if (input.getAttribute('form')) hidden.setAttribute('form', input.getAttribute('form'));
        input.after(hidden);
        input.removeAttribute('name');
        ['min', 'max', 'step'].forEach((a) => input.removeAttribute(a));
        input.type = 'text';
        const sync = () => (hidden.value = digitsOf(input.value));
        input.addEventListener('input', sync);
        mask(input, FORMATS.money);
        sync();
    }

    // ---------------------------------------------------------------------
    // 날짜·월·일시
    // ---------------------------------------------------------------------
    const KINDS = {
        date: {placeholder: 'YYYY-MM-DD', parse: parseDate, display: (v) => v, value: (v) => v},
        month: {placeholder: 'YYYY-MM', parse: parseMonth, display: (v) => v, value: (v) => v},
        'datetime-local': {
            placeholder: 'YYYY-MM-DD HH:MM', parse: parseDateTime,
            display: (v) => v.replace('T', ' ').slice(0, 16), value: (v) => v.replace(' ', 'T')
        }
    };

    function validDate(y, m, d) {
        const date = new Date(y, m - 1, d);
        return date.getFullYear() === y && date.getMonth() === m - 1 && date.getDate() === d ? isoDate(date) : null;
    }

    /** 2026-10-05, 2026.10.5, 2026/10/05, 20261005 → 2026-10-05 */
    function parseDate(text) {
        const t = text.trim();
        const m = t.match(/^(\d{4})(\d{2})(\d{2})$/) || t.match(/^(\d{4})\D+(\d{1,2})\D+(\d{1,2})\D*$/);
        return m ? validDate(+m[1], +m[2], +m[3]) : null;
    }

    /** 2026-10, 2026.10, 202610, 2026년 10월 → 2026-10 */
    function parseMonth(text) {
        const t = text.trim();
        const m = t.match(/^(\d{4})(\d{2})$/) || t.match(/^(\d{4})\D+(\d{1,2})\D*$/);
        return m && +m[2] >= 1 && +m[2] <= 12 ? `${m[1]}-${pad(+m[2])}` : null;
    }

    /** 날짜 + (선택) 시각. 시각을 비우면 00:00 */
    function parseDateTime(text) {
        const t = text.trim().replace('T', ' ');
        const m = t.match(/^(.+?)(?:\s+(\d{1,2}):?(\d{2}))?$/);
        if (!m) return null;
        const date = parseDate(m[1]);
        if (!date) return null;
        const h = m[2] === undefined ? 0 : +m[2];
        const min = m[3] === undefined ? 0 : +m[3];
        return h < 24 && min < 60 ? `${date} ${pad(h)}:${pad(min)}` : null;
    }

    function dateField(input) {
        const kind = input.type;
        const spec = KINDS[kind];
        const compact = !input.classList.contains('input');
        const hidden = el('input');
        hidden.type = 'hidden';
        hidden.name = input.name;
        if (input.getAttribute('form')) hidden.setAttribute('form', input.getAttribute('form'));
        const initial = input.value ? spec.display(input.value) : '';
        const min = input.min ? spec.display(input.min) : null;
        const max = input.max ? spec.display(input.max) : null;

        const hadFocus = document.activeElement === input;
        const field = el('div', compact ? 'date-field date-field-compact' : 'date-field');
        // 폭을 정하는 클래스는 바깥 상자로 옮긴다.
        [...input.classList].filter((c) => /^(w-|max-w-|min-w-|flex-|shrink|grow)/.test(c)).forEach((c) => {
            input.classList.remove(c);
            field.classList.add(c);
        });
        input.before(field);
        field.append(input, hidden);
        input.removeAttribute('name');
        input.removeAttribute('min');
        input.removeAttribute('max');
        input.type = 'text';
        input.value = initial;
        input.dataset.dateKind = kind;
        input.spellcheck = false;
        input.placeholder = spec.placeholder;
        input.setAttribute('aria-haspopup', 'dialog');
        input.setAttribute('aria-expanded', 'false');
        hidden.value = initial ? spec.value(initial) : '';
        // DOM 에서 옮기면 포커스가 풀린다. (모달의 autofocus 등)
        if (hadFocus) input.focus({preventScroll: true});

        if (!compact) {
            const toggle = el('button', 'date-field-toggle');
            toggle.type = 'button';
            toggle.tabIndex = -1;
            toggle.setAttribute('aria-label', '달력 열기');
            toggle.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="size-4" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5"/></svg>';
            toggle.addEventListener('click', () => (isOpen() ? closeFloating() : open(true)));
            field.append(toggle);
        }

        const isOpen = () => floating && floating.anchor === field;

        function outOfRange(v) {
            // 같은 형식(YYYY-MM-DD[ HH:MM]) 문자열은 사전순 비교가 곧 날짜 비교다.
            const key = kind === 'datetime-local' ? v.slice(0, 10) : v;
            if (min && key < min.slice(0, key.length)) return `${min} 이후로 입력하세요.`;
            if (max && key > max.slice(0, key.length)) return `${max} 이전으로 입력하세요.`;
            return '';
        }

        /** 입력 중: 올바른 값이면 hidden 에 반영, 아니면 비우고 이유를 남긴다. */
        function sync() {
            const text = input.value.trim();
            if (!text) {
                hidden.value = '';
                input.setCustomValidity('');
                return null;
            }
            const parsed = spec.parse(text);
            if (!parsed) {
                hidden.value = '';
                input.setCustomValidity(`날짜 형식이 올바르지 않습니다. (예: ${spec.placeholder})`);
                return null;
            }
            const range = outOfRange(parsed);
            input.setCustomValidity(range);
            hidden.value = range ? '' : spec.value(parsed);
            return parsed;
        }

        function commit(value) {
            input.value = value;
            sync();
            input.dispatchEvent(new Event('change', {bubbles: true}));
        }

        mask(input, FORMATS[kind]);
        input.addEventListener('input', sync);
        input.addEventListener('change', () => {
            const parsed = sync();
            if (parsed && input.value !== parsed) input.value = parsed;
        });
        input.addEventListener('click', () => {
            if (compact && !isOpen()) open();
        });
        input.addEventListener('keydown', (e) => {
            if ((e.key === 'ArrowDown' && (e.altKey || !isOpen())) && !e.isComposing) {
                e.preventDefault();
                open(true);
            }
        });

        // ---- 달력 ----
        function open(focusGrid) {
            const current = spec.parse(input.value) || '';
            const today = new Date();
            const state = {
                cursor: current ? new Date(+current.slice(0, 4), +current.slice(5, 7) - 1, kind === 'month' ? 1 : +current.slice(8, 10)) : today,
                time: kind === 'datetime-local' ? (current.slice(11, 16) || '09:00') : null
            };
            const panel = el('div', 'calendar');
            panel.setAttribute('role', 'dialog');
            panel.setAttribute('aria-label', kind === 'month' ? '월 선택' : '날짜 선택');
            panel.addEventListener('keydown', (e) => onKey(e, state, panel));
            render(panel, state, current);
            input.setAttribute('aria-expanded', 'true');
            openFloating(field, panel, () => input.setAttribute('aria-expanded', 'false'));
            if (focusGrid) focusCursor(panel);
        }

        function choose(value) {
            closeFloating();
            commit(value);
            input.focus({preventScroll: true});
        }

        function disabled(key) {
            return (min && key < min.slice(0, key.length)) || (max && key > max.slice(0, key.length));
        }

        function render(panel, state, current) {
            panel.replaceChildren();
            const y = state.cursor.getFullYear();
            const m = state.cursor.getMonth();
            const head = el('div', 'calendar-head');
            const prev = navButton('M15.75 19.5 8.25 12l7.5-7.5', kind === 'month' ? '이전 해' : '이전 달');
            const next = navButton('m8.25 4.5 7.5 7.5-7.5 7.5', kind === 'month' ? '다음 해' : '다음 달');
            const step = (n) => {
                state.cursor = kind === 'month' ? new Date(y + n, m, 1) : new Date(y, m + n, 1);
                render(panel, state, current);
            };
            prev.addEventListener('click', () => step(-1));
            next.addEventListener('click', () => step(1));

            const titles = el('div', 'calendar-title');
            titles.append(yearSelect(y, (v) => {
                state.cursor = new Date(v, m, 1);
                render(panel, state, current);
                panel.querySelector('.calendar-title select').focus();
            }));
            if (kind !== 'month') {
                const months = el('select', 'calendar-select');
                months.setAttribute('aria-label', '월');
                for (let i = 0; i < 12; i++) months.append(new Option(`${i + 1}월`, i, false, i === m));
                months.addEventListener('change', () => {
                    state.cursor = new Date(y, +months.value, 1);
                    render(panel, state, current);
                    panel.querySelectorAll('.calendar-title select')[1].focus();
                });
                titles.append(months);
            }
            head.append(prev, titles, next);
            panel.append(head);

            if (kind === 'month') {
                const grid = el('div', 'calendar-months');
                const thisMonth = isoDate(new Date()).slice(0, 7);
                for (let i = 0; i < 12; i++) {
                    const key = `${y}-${pad(i + 1)}`;
                    const b = cell(`${i + 1}월`, key, key === current, key === thisMonth, false);
                    b.dataset.index = i;
                    b.tabIndex = i === m ? 0 : -1;
                    grid.append(b);
                }
                panel.append(grid);
                footer(panel, '이번 달', thisMonth);
                return;
            }

            const grid = el('div', 'calendar-days');
            WEEKDAYS.forEach((w, i) => grid.append(el('span', 'calendar-weekday' + (i === 0 ? ' text-fg-critical' : ''), w)));
            const first = new Date(y, m, 1);
            const start = new Date(y, m, 1 - first.getDay());
            const today = isoDate(new Date());
            const selectedDate = current ? current.slice(0, 10) : '';
            const cursorKey = isoDate(state.cursor);
            for (let i = 0; i < 42; i++) {
                const day = new Date(start.getFullYear(), start.getMonth(), start.getDate() + i);
                const key = isoDate(day);
                const b = cell(String(day.getDate()), key, key === selectedDate, key === today, day.getMonth() !== m);
                b.tabIndex = key === cursorKey ? 0 : -1;
                if (day.getDay() === 0) b.classList.add('is-sunday');
                grid.append(b);
                if (i === 34 && new Date(y, m + 1, 0).getDate() + first.getDay() <= 35) break;
            }
            panel.append(grid);
            if (kind === 'datetime-local') {
                const row = el('div', 'calendar-time');
                row.append(el('span', '', '시각'));
                const time = el('input', 'input filter-input w-20 tabular-nums');
                time.value = state.time;
                time.setAttribute('aria-label', '시각 (HH:MM)');
                time.placeholder = 'HH:MM';
                time.inputMode = 'numeric';
                time.addEventListener('change', () => {
                    const m2 = time.value.trim().match(/^(\d{1,2}):?(\d{2})$/);
                    if (m2 && +m2[1] < 24 && +m2[2] < 60) {
                        state.time = `${pad(+m2[1])}:${pad(+m2[2])}`;
                        if (selectedDate) commit(`${selectedDate} ${state.time}`);
                    }
                    time.value = state.time;
                });
                row.append(time, el('span', 'calendar-hint', '날짜를 누르면 이 시각으로 입력됩니다.'));
                panel.append(row);
            }
            footer(panel, '오늘', today);

            function cell(label, key, selected, isToday, outside) {
                const b = el('button', 'calendar-cell', label);
                b.type = 'button';
                b.dataset.key = key;
                if (selected) b.setAttribute('aria-selected', 'true');
                if (isToday) b.classList.add('is-today');
                if (outside) b.classList.add('is-outside');
                if (disabled(key)) b.disabled = true;
                b.addEventListener('click', () => choose(kind === 'datetime-local' ? `${key} ${state.time}` : key));
                return b;
            }
        }

        function footer(panel, todayLabel, todayKey) {
            const foot = el('div', 'calendar-foot');
            const today = el('button', 'calendar-link', todayLabel);
            today.type = 'button';
            today.disabled = !!disabled(todayKey);
            today.addEventListener('click', () => choose(kind === 'datetime-local' ? `${todayKey} ${pad(new Date().getHours())}:00` : todayKey));
            foot.append(today);
            if (!input.required && input.value) {
                const clear = el('button', 'calendar-link text-fg-neutral-subtle', '지우기');
                clear.type = 'button';
                clear.addEventListener('click', () => choose(''));
                foot.append(clear);
            }
            panel.append(foot);
        }

        function yearSelect(y, onChange) {
            const select = el('select', 'calendar-select');
            select.setAttribute('aria-label', '연도');
            const now = new Date().getFullYear();
            const from = Math.min(y, min ? +min.slice(0, 4) : now - 90);
            const to = Math.max(y, max ? +max.slice(0, 4) : now + 10);
            for (let v = to; v >= from; v--) select.append(new Option(`${v}년`, v, false, v === y));
            select.addEventListener('change', () => onChange(+select.value));
            return select;
        }

        function navButton(path, label) {
            const b = el('button', 'calendar-nav');
            b.type = 'button';
            b.setAttribute('aria-label', label);
            b.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="size-4" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="${path}"/></svg>`;
            return b;
        }

        function focusCursor(panel) {
            const target = panel.querySelector('.calendar-cell[tabindex="0"]');
            if (target) target.focus({preventScroll: true});
        }

        /** 방향키로 날짜(월) 이동, PageUp/PageDown 으로 달(해) 이동 */
        function onKey(e, state, panel) {
            if (!e.target.classList.contains('calendar-cell')) return;
            const monthMode = kind === 'month';
            const moves = monthMode
                ? {ArrowLeft: -1, ArrowRight: 1, ArrowUp: -3, ArrowDown: 3, PageUp: -12, PageDown: 12}
                : {ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7};
            const c = state.cursor;
            let next;
            if (moves[e.key] !== undefined) {
                next = monthMode ? new Date(c.getFullYear(), c.getMonth() + moves[e.key], 1)
                    : new Date(c.getFullYear(), c.getMonth(), c.getDate() + moves[e.key]);
            } else if (!monthMode && (e.key === 'PageUp' || e.key === 'PageDown')) {
                next = new Date(c.getFullYear(), c.getMonth() + (e.key === 'PageUp' ? -1 : 1), Math.min(c.getDate(), 28));
            } else {
                return;
            }
            e.preventDefault();
            state.cursor = next;
            render(panel, state, spec.parse(input.value) || '');
            focusCursor(panel);
        }
    }

    // ---------------------------------------------------------------------
    // 검색 선택
    // ---------------------------------------------------------------------
    const CHO = 'ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ';

    function initials(text) {
        let out = '';
        for (const ch of text) {
            const code = ch.charCodeAt(0) - 0xAC00;
            out += code >= 0 && code <= 11171 ? CHO[Math.floor(code / 588)] : ch;
        }
        return out;
    }

    const isJamo = (token) => /^[ㄱ-ㅎ]+$/.test(token);

    let comboSeq = 0;

    function combobox(select) {
        const required = select.required;
        const options = [...select.options]
            .filter((o) => o.value !== '' || !required)
            .map((o) => {
                const label = o.textContent.trim();
                return {value: o.value, label, depth: o.textContent.length - o.textContent.trimStart().length,
                    search: label.toLowerCase(), initials: initials(label)};
            });
        const empty = [...select.options].find((o) => o.value === '');
        const listId = 'combobox-list-' + (++comboSeq);

        const field = el('div', 'combobox');
        [...select.classList].filter((c) => /^(w-|max-w-|min-w-|flex-|shrink|grow)/.test(c)).forEach((c) => field.classList.add(c));
        const input = el('input', select.classList.contains('filter-select') ? 'input filter-input' : 'input');
        input.type = 'text';
        input.autocomplete = 'off';
        input.spellcheck = false;
        input.setAttribute('role', 'combobox');
        input.setAttribute('aria-autocomplete', 'list');
        input.setAttribute('aria-expanded', 'false');
        input.setAttribute('aria-controls', listId);
        input.placeholder = empty ? empty.textContent.trim() : '검색해서 선택';
        if (select.id) {
            input.id = select.id;
            select.id = select.id + '-select';
        }
        ['aria-label', 'aria-describedby', 'aria-invalid'].forEach((a) => select.hasAttribute(a) && input.setAttribute(a, select.getAttribute(a)));
        if (select.autofocus) input.autofocus = true;
        input.required = required;
        select.required = false;
        select.tabIndex = -1;
        select.setAttribute('aria-hidden', 'true');
        select.classList.add('sr-only');
        const hadFocus = document.activeElement === select;
        select.before(field);
        field.append(input, select);
        input.insertAdjacentHTML('afterend', '<span class="combobox-chevron" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="size-4"><path stroke-linecap="round" stroke-linejoin="round" d="m6 9 6 6 6-6"/></svg></span>');

        const labelOf = (value) => (options.find((o) => o.value === value) || {label: ''}).label;
        const reset = () => {
            input.value = select.value ? labelOf(select.value) : '';
            input.setCustomValidity('');
        };
        reset();
        if (hadFocus) input.focus({preventScroll: true});

        let matches = [];
        let active = -1;
        let list = null;

        function filter(text) {
            const tokens = text.trim().toLowerCase().split(/\s+/).filter(Boolean);
            if (!tokens.length) return options;
            return options.filter((o) => tokens.every((t) => o.search.includes(t) || (isJamo(t) && o.initials.includes(t))));
        }

        function open(text) {
            matches = filter(text).slice(0, 200);
            const current = matches.findIndex((o) => o.value === select.value);
            active = text ? (matches.length ? 0 : -1) : current;
            if (!list || !floating || floating.panel !== list) {
                list = el('ul', 'combobox-list');
                list.id = listId;
                list.setAttribute('role', 'listbox');
                list.addEventListener('pointerdown', (e) => e.preventDefault());
                list.addEventListener('click', (e) => {
                    const item = e.target.closest('[data-index]');
                    if (item) pick(matches[+item.dataset.index]);
                });
                input.setAttribute('aria-expanded', 'true');
                openFloating(field, list, () => {
                    input.setAttribute('aria-expanded', 'false');
                    input.removeAttribute('aria-activedescendant');
                }, reset);
            }
            draw();
            placeFloating();
        }

        function draw() {
            list.replaceChildren();
            if (!matches.length) {
                list.append(el('li', 'combobox-empty', '일치하는 항목이 없습니다.'));
                return;
            }
            matches.forEach((o, i) => {
                const item = el('li', 'combobox-option', o.label || '(선택 안 함)');
                item.id = `${listId}-${i}`;
                item.dataset.index = i;
                item.setAttribute('role', 'option');
                if (o.depth) item.style.paddingLeft = `calc(var(--seed-dimension-x3) + ${Math.min(o.depth, 8) * 0.5}rem)`;
                if (o.value === select.value) item.setAttribute('aria-selected', 'true');
                if (i === active) item.classList.add('is-active');
                list.append(item);
            });
            if (active >= 0) {
                const item = list.children[active];
                input.setAttribute('aria-activedescendant', item.id);
                // scrollIntoView 는 바깥 스크롤까지 움직이므로 목록 안에서만 맞춘다.
                if (item.offsetTop < list.scrollTop) list.scrollTop = item.offsetTop;
                else if (item.offsetTop + item.offsetHeight > list.scrollTop + list.clientHeight) list.scrollTop = item.offsetTop + item.offsetHeight - list.clientHeight;
            }
        }

        function pick(option) {
            closeFloating();
            if (!option) return;
            const changed = select.value !== option.value;
            select.value = option.value;
            reset();
            if (changed) select.dispatchEvent(new Event('change', {bubbles: true}));
        }

        const isOpen = () => floating && floating.panel === list;

        input.addEventListener('focus', () => input.select());
        input.addEventListener('click', () => (isOpen() ? closeFloating() : open('')));
        input.addEventListener('input', () => {
            input.setCustomValidity(input.value.trim() && input.value !== labelOf(select.value) ? '목록에서 선택하세요.' : '');
            open(input.value);
        });
        input.addEventListener('keydown', (e) => {
            if (e.isComposing) return;
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault();
                if (!isOpen()) return open('');
                if (!matches.length) return;
                active = (active + (e.key === 'ArrowDown' ? 1 : -1) + matches.length) % matches.length;
                draw();
            } else if (e.key === 'Enter' && isOpen()) {
                e.preventDefault();
                pick(matches[active]);
            }
        });
        input.addEventListener('blur', () => {
            if (isOpen()) closeFloating();
            if (!input.value.trim() && !required && select.value !== '') {
                pick(options.find((o) => o.value === '') || null);
            }
            reset();
        });
        select.addEventListener('change', reset);
    }

    // ---------------------------------------------------------------------
    // 파일 놓기
    // ---------------------------------------------------------------------
    function sizeLabel(size) {
        if (size < 1024) return size + ' B';
        if (size < 1024 * 1024) return Math.round(size / 1024) + ' KB';
        return (size / 1024 / 1024).toFixed(1) + ' MB';
    }

    function fileDrop(zone) {
        const input = zone.querySelector('input[type=file]');
        const text = zone.querySelector('[data-file-drop-text]');
        const idle = text.innerHTML;
        const maxSize = Number(zone.dataset.maxSize || 0);
        const allowed = (input.accept || '').split(',').map((s) => s.trim().toLowerCase()).filter((s) => s.startsWith('.'));

        function show() {
            const file = input.files[0];
            zone.classList.toggle('has-file', !!file);
            if (!file) {
                text.innerHTML = idle;
                return;
            }
            const ext = '.' + (file.name.split('.').pop() || '').toLowerCase();
            let problem = '';
            if (allowed.length && !allowed.includes(ext)) problem = '첨부할 수 없는 형식입니다.';
            else if (maxSize && file.size > maxSize) problem = `${sizeLabel(maxSize)} 이하만 첨부할 수 있습니다.`;
            input.setCustomValidity(problem);
            zone.classList.toggle('is-invalid', !!problem);
            text.replaceChildren(el('span', 'truncate t4-medium text-fg-neutral', file.name),
                el('span', problem ? 'shrink-0 text-fg-critical' : 'shrink-0 tabular-nums', problem || sizeLabel(file.size)));
        }

        input.addEventListener('change', show);
        zone.addEventListener('dragover', (e) => {
            e.preventDefault();
            zone.classList.add('is-dragging');
        });
        zone.addEventListener('dragleave', (e) => {
            if (!zone.contains(e.relatedTarget)) zone.classList.remove('is-dragging');
        });
        zone.addEventListener('drop', (e) => {
            e.preventDefault();
            zone.classList.remove('is-dragging');
            if (!e.dataTransfer.files.length) return;
            const transfer = new DataTransfer();
            transfer.items.add(e.dataTransfer.files[0]);
            input.files = transfer.files;
            input.dispatchEvent(new Event('change', {bubbles: true}));
        });
        input.form?.addEventListener('reset', () => setTimeout(show));
        show();
    }

    // 숫자 입력: 포커스 중 휠을 굴리면 값 대신 페이지가 스크롤되게 포커스를 뗀다.
    document.addEventListener('wheel', (e) => {
        if (e.target === document.activeElement && e.target.matches('input[type=number]')) e.target.blur();
    }, {passive: true});

    function enhance(root) {
        root.querySelectorAll('input[type=date], input[type=month], input[type=datetime-local]').forEach((input) => {
            if (!input.hasAttribute('data-native')) dateField(input);
        });
        root.querySelectorAll('input[data-money]:not([data-ready]), input[data-mask=money]:not([data-ready])').forEach((input) => {
            input.dataset.ready = 'true';
            moneyField(input);
        });
        root.querySelectorAll('input[type=tel]:not([data-ready]), input[data-mask]:not([data-ready])').forEach((input) => {
            input.dataset.ready = 'true';
            const kind = input.dataset.mask || 'phone';
            mask(input, FORMATS[kind], VALIDATORS[kind]);
        });
        root.querySelectorAll('select[data-combobox]:not([data-ready])').forEach((select) => {
            select.dataset.ready = 'true';
            combobox(select);
        });
        root.querySelectorAll('[data-file-drop]:not([data-ready])').forEach((zone) => {
            zone.dataset.ready = 'true';
            fileDrop(zone);
        });
    }

    htmx.onLoad((node) => enhance(node));
})();
