<template>
    <div class="markdown-content" v-html="formattedContent" />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { marked } from 'marked';
import { codeToHtml } from 'shiki';
import he from 'he';
import { useMutationObserver } from '@vueuse/core';

const props = defineProps<{
    content: string;
}>();

// Configure marked
marked.setOptions({
    breaks: true,
    gfm: true,
});

// Shiki 캐시
const highlightCache = new Map<string, string>();

// 다크 모드 상태 (반응형)
const isDarkMode = ref(false);

// 초기 상태 설정 및 변경 감지
if (typeof document !== 'undefined') {
    isDarkMode.value = document.documentElement.classList.contains('dark');

    useMutationObserver(
        document.documentElement,
        () => {
            const newIsDarkMode = document.documentElement.classList.contains('dark');
            if (isDarkMode.value !== newIsDarkMode) {
                isDarkMode.value = newIsDarkMode;
            }
        },
        {
            attributes: true,
            attributeFilter: ['class'],
        }
    );
}

const formattedContent = ref('');

// Shiki로 코드 하이라이트
const highlightWithShiki = async (code: string, lang: string): Promise<string> => {
    const cacheKey = `${lang}:${code}:${isDarkMode.value ? 'dark' : 'light'}`;
    if (highlightCache.has(cacheKey)) {
        return highlightCache.get(cacheKey)!;
    }

    try {
        const langMap: Record<string, string> = {
            'js': 'javascript',
            'ts': 'typescript',
            'py': 'python',
            'sh': 'bash',
            'shell': 'bash',
            'kt': 'kotlin',
        };

        const shikiLang = langMap[lang.toLowerCase()] || lang.toLowerCase();
        // 테마 변경: 라이트 모드는 'github-light', 다크 모드는 'one-dark-pro' (nsales-pro와 동일)
        const theme = isDarkMode.value ? 'one-dark-pro' : 'github-light';

        const html = await codeToHtml(code, {
            lang: shikiLang,
            theme: theme,
        });

        highlightCache.set(cacheKey, html);
        return html;
    } catch (error) {
        // Fallback
        try {
            const theme = isDarkMode.value ? 'one-dark-pro' : 'github-light';
            const html = await codeToHtml(code, {
                lang: 'text',
                theme: theme,
            });
            highlightCache.set(cacheKey, html);
            return html;
        } catch {
            const fallback = `<pre><code>${he.encode(code)}</code></pre>`;
            highlightCache.set(cacheKey, fallback);
            return fallback;
        }
    }
};

// 컨텐츠 처리
const processContent = async () => {
    try {
        let html = marked(props.content) as string;

        // 코드 블록 찾기
        const codeBlockRegex = /<pre><code(?:\s+class="([^"]*)")?[^>]*>([\s\S]*?)<\/code><\/pre>/g;
        const codeBlocks: Array<{ match: string; code: string; lang: string }> = [];
        let match;

        while ((match = codeBlockRegex.exec(html)) !== null) {
            const [fullMatch, classAttr, code] = match;
            const langMatch = classAttr?.match(/(?:^|\s)language-(\w+)(?:\s|$)/);
            const lang = langMatch?.[1] || 'text';

            // HTML 디코딩
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = code ?? '';
            const plainCode = (tempDiv.textContent ?? tempDiv.innerText ?? '') as string;

            codeBlocks.push({ match: fullMatch, code: plainCode, lang });
        }

        // 각 코드 블록을 Shiki로 처리
        for (const block of codeBlocks) {
            const highlightedHtml = await highlightWithShiki(block.code, block.lang);

            const wrappedCode = `
        <div class="code-block-container" data-language="${block.lang}">
          <div class="code-block-header">
            <span class="code-language">${block.lang.toUpperCase()}</span>
          </div>
          <div class="shiki-wrapper">${highlightedHtml}</div>
        </div>
      `;

            html = html.replace(block.match, wrappedCode);
        }

        formattedContent.value = html;
    } catch (error) {
        console.error('Content processing error:', error);
        formattedContent.value = marked(props.content) as string;
    }
};

// 메시지 내용이 변경될 때마다 처리
watch(() => props.content, processContent, { immediate: true });

// 다크 모드 변경 시 재처리
watch(isDarkMode, () => {
    highlightCache.clear(); // 캐시 초기화하여 새로운 테마 적용
    processContent();
});
</script>

<style>
.markdown-content {
    color: var(--foreground);
    line-height: 1.7;
    font-size: 0.95rem;
}

/* 제목 스타일 - 그라데이션과 마진 최적화 */
.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
    background: linear-gradient(135deg, var(--primary) 0%, color-mix(in oklch, var(--primary), transparent 20%) 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    font-weight: 700;
    margin: 1.5em 0 0.75em 0;
    line-height: 1.3;
    letter-spacing: -0.025em;
}

.markdown-content h1 {
    font-size: 1.75rem;
}

.markdown-content h2 {
    font-size: 1.5rem;
}

.markdown-content h3 {
    font-size: 1.25rem;
}

.markdown-content h4 {
    font-size: 1.125rem;
}

.markdown-content h5 {
    font-size: 1rem;
}

.markdown-content h6 {
    font-size: 0.9rem;
}

/* 첫 번째 제목의 상단 마진 제거 */
.markdown-content h1:first-child,
.markdown-content h2:first-child,
.markdown-content h3:first-child,
.markdown-content h4:first-child,
.markdown-content h5:first-child,
.markdown-content h6:first-child {
    margin-top: 0;
}

/* 강조 텍스트 */
.markdown-content strong {
    color: var(--foreground);
    font-weight: 650;
    background: color-mix(in oklch, var(--primary), transparent 90%);
    padding: 0.125rem 0.25rem;
    border-radius: 0.25rem;
}

.markdown-content em {
    color: var(--muted-foreground);
    font-style: italic;
}

/* 문단 스타일 */
.markdown-content p {
    margin: 1em 0;
    color: var(--foreground);
    line-height: 1.7;
}

.markdown-content p:first-child {
    margin-top: 0;
}

.markdown-content p:last-child {
    margin-bottom: 0;
}

/* 리스트 스타일 - 더 모던한 불릿과 간격 */
.markdown-content ul,
.markdown-content ol {
    margin: 1.25em 0;
    padding-left: 1.75rem;
}

.markdown-content ul {
    list-style: none;
}

.markdown-content ul li {
    position: relative;
    margin: 0.75em 0;
    line-height: 1.6;
}

.markdown-content ul li::before {
    content: '';
    position: absolute;
    left: -1.25rem;
    top: 0.7em;
    width: 6px;
    height: 6px;
    background: linear-gradient(135deg, var(--primary) 0%, color-mix(in oklch, var(--primary), transparent 30%) 100%);
    border-radius: 50%;
    transform: translateY(-50%);
}

.markdown-content ol li {
    margin: 0.75em 0;
    line-height: 1.6;
}

.markdown-content ol {
    list-style-type: decimal;
    list-style-position: outside;
}

.markdown-content ol li::marker {
    color: var(--primary);
    font-weight: 600;
}

/* 중첩 리스트 */
.markdown-content ul ul,
.markdown-content ol ol,
.markdown-content ul ol,
.markdown-content ol ul {
    margin: 0.5em 0;
}

.markdown-content ol ol {
    counter-reset: list-counter;
}

.markdown-content ol ol li::before {
    left: -1.5rem;
}

/* 인용문 - 글래스모피즘 효과 */
.markdown-content blockquote {
    position: relative;
    margin: 1.5em 0;
    padding: 1.25rem 1.5rem;
    background: color-mix(in oklch, var(--muted), transparent 50%);
    backdrop-filter: blur(8px);
    border-left: 4px solid var(--primary);
    border-radius: 0 0.75rem 0.75rem 0;
    font-style: italic;
    color: var(--muted-foreground);
    box-shadow: var(--shadow-md);
}

.markdown-content blockquote::before {
    content: '"';
    position: absolute;
    top: -0.5rem;
    left: 1rem;
    font-size: 2rem;
    color: color-mix(in oklch, var(--primary), transparent 40%);
    font-weight: bold;
}

.markdown-content blockquote p {
    margin: 0.5em 0;
}

.markdown-content blockquote p:first-child {
    margin-top: 0;
}

.markdown-content blockquote p:last-child {
    margin-bottom: 0;
}

/* 인라인 코드 - 더 예쁜 배경과 타이포그래피 */
.markdown-content code:not(pre code) {
    background: linear-gradient(135deg, var(--muted) 0%, color-mix(in oklch, var(--muted), transparent 20%) 100%);
    color: var(--primary);
    padding: 0.2rem 0.4rem;
    border-radius: 0.375rem;
    font-size: 0.875rem;
    font-family: var(--font-mono, ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace);
    font-weight: 500;
    border: 1px solid color-mix(in oklch, var(--border), transparent 50%);
    box-shadow: var(--shadow-xs);
}

/* 헤더 내 인라인 코드 - 가독성 개선 */
.markdown-content h1 code:not(pre code),
.markdown-content h2 code:not(pre code),
.markdown-content h3 code:not(pre code),
.markdown-content h4 code:not(pre code),
.markdown-content h5 code:not(pre code),
.markdown-content h6 code:not(pre code) {
    -webkit-text-fill-color: var(--background) !important;
    background: var(--primary) !important;
    color: var(--primary-foreground) !important;
    border: 1px solid color-mix(in oklch, var(--primary), transparent 30%) !important;
    font-weight: 600;
    display: inline-block;
    vertical-align: baseline;
    margin: 0 0.125rem;
}

/* 코드 블록 컨테이너 - 모던한 디자인과 복사 기능 */
.markdown-content .code-block-container {
    position: relative;
    margin: 1.5em 0;
    background: color-mix(in oklch, var(--background), var(--foreground) 3%);
    border: 1px solid color-mix(in oklch, var(--border), var(--foreground) 20%);
    border-radius: 0.75rem;
    overflow: hidden;
    box-shadow: 0 4px 6px -1px color-mix(in oklch, var(--foreground), transparent 90%), 0 2px 4px -1px color-mix(in oklch, var(--foreground), transparent 94%);
    backdrop-filter: blur(8px);
}

.markdown-content .code-block-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 1rem;
    background: color-mix(in oklch, var(--background), var(--foreground) 5%);
    border-bottom: 1px solid color-mix(in oklch, var(--border), var(--foreground) 30%);
    backdrop-filter: blur(4px);
}

.markdown-content .code-language {
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--muted-foreground);
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.markdown-content .code-language {
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--muted-foreground);
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.markdown-content .copy-code-btn {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    padding: 0.375rem 0.75rem;
    background: var(--background);
    border: 1px solid var(--border);
    border-radius: 0.375rem;
    color: var(--muted-foreground);
    font-size: 0.75rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    user-select: none;
}

.markdown-content .copy-code-btn:hover {
    background: var(--muted);
    color: var(--foreground);
    border-color: var(--border);
    transform: translateY(-1px);
    box-shadow: var(--shadow-sm);
}

.markdown-content .copy-code-btn.copied {
    background: var(--primary);
    color: var(--primary-foreground);
    border-color: var(--primary);
}

.markdown-content .copy-code-btn svg {
    width: 14px;
    height: 14px;
    transition: transform 0.2s ease;
}

.markdown-content .copy-code-btn:hover svg {
    transform: scale(1.1);
}

.markdown-content .code-block-container pre {
    margin: 0;
    padding: 1.5rem;
    background: transparent;
    border: none;
    border-radius: 0;
    box-shadow: none;
    tab-size: 4;
    -moz-tab-size: 4;
    -o-tab-size: 4;
}

.markdown-content .code-block-container pre code {
    background: transparent;
    padding: 0;
    border: none;
    font-size: 0.875rem;
    color: var(--foreground);
    /* Slate-800 (어두운 색상) */
    box-shadow: none;
    font-family: var(--font-mono, ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace);
    tab-size: 4;
    -moz-tab-size: 4;
    -o-tab-size: 4;
}

/* Shiki 래퍼 스타일 - Shiki는 인라인 스타일을 사용하므로 래퍼만 설정 */
.markdown-content .shiki-wrapper {
    overflow-x: auto;
    border-radius: 0.5rem;
}

.markdown-content .shiki-wrapper pre {
    margin: 0 !important;
    padding: 1.5rem !important;
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
    overflow-x: auto !important;
    font-family: var(--font-mono, ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace) !important;
    line-height: 1.5 !important;
    tab-size: 4 !important;
    -moz-tab-size: 4 !important;
    -o-tab-size: 4 !important;
}

.markdown-content .shiki-wrapper code {
    background: transparent !important;
    padding: 0 !important;
    border: none !important;
    box-shadow: none !important;
    font-family: inherit !important;
}

/* 다크 테마에서 더 잘 보이도록 배경색 및 텍스트 조정 */
.dark .markdown-content {
    color: hsl(var(--foreground));
}

.dark .markdown-content p {
    color: hsl(var(--foreground) / 0.9);
}

.dark .markdown-content strong {
    color: hsl(var(--foreground));
    background: hsl(var(--primary) / 0.2);
}

.dark .markdown-content .code-block-container {
    background: hsl(var(--muted) / 0.3);
    border-color: hsl(var(--border) / 0.4);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
}

.dark .markdown-content .code-block-header {
    background: hsl(var(--muted) / 0.5);
    border-bottom-color: hsl(var(--border) / 0.4);
}

.dark .markdown-content code:not(pre code) {
    background: hsl(var(--muted) / 0.4);
    color: hsl(var(--primary) / 0.9);
    border-color: hsl(var(--border) / 0.3);
}

/* 다크 모드 테이블 스타일 강화 */
.dark .markdown-content table {
    border-color: hsl(var(--border) / 0.3);
}

.dark .markdown-content th {
    background: hsl(var(--muted) / 0.4);
    color: hsl(var(--foreground));
    border-bottom-color: hsl(var(--border) / 0.4);
}

.dark .markdown-content td {
    background: transparent;
    border-bottom-color: hsl(var(--border) / 0.2);
    color: hsl(var(--foreground) / 0.9);
}

.dark .markdown-content tr:hover td {
    background: hsl(var(--muted) / 0.2);
}

/* 다크 모드 인용문 */
.dark .markdown-content blockquote {
    background: hsl(var(--muted) / 0.2);
    color: hsl(var(--muted-foreground));
    border-left-color: hsl(var(--primary) / 0.7);
}

/* 다크 모드 링크 - 가독성 개선 */
.dark .markdown-content a {
    color: #60a5fa;
    /* 밝은 파란색으로 변경하여 가독성 확보 */
    text-decoration: underline;
    text-decoration-color: rgba(96, 165, 250, 0.3);
    text-underline-offset: 4px;
    font-weight: 600;
}

.dark .markdown-content a:hover {
    color: #93c5fd;
    text-decoration-color: #93c5fd;
    background: rgba(96, 165, 250, 0.1);
    border-radius: 2px;
}

/* 다크 모드 헤더 */
.dark .markdown-content h1,
.dark .markdown-content h2,
.dark .markdown-content h3,
.dark .markdown-content h4,
.dark .markdown-content h5,
.dark .markdown-content h6 {
    color: hsl(var(--foreground));
    background: none;
    -webkit-text-fill-color: initial;
}

/* 기존 pre 스타일은 코드 블록 컨테이너가 없는 경우를 위해 유지 */
.markdown-content pre:not(.code-block-container pre) {
    position: relative;
    margin: 1.5em 0;
    padding: 1.5rem;
    background: linear-gradient(135deg, var(--muted) 0%, color-mix(in oklch, var(--muted), transparent 10%) 100%);
    border: 1px solid var(--border);
    border-radius: 0.75rem;
    overflow-x: auto;
    box-shadow: 0 4px 6px -1px color-mix(in oklch, var(--foreground), transparent 90%), 0 2px 4px -1px color-mix(in oklch, var(--foreground), transparent 94%);
    backdrop-filter: blur(8px);
    font-family: var(--font-mono, ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace);
    tab-size: 4;
    -moz-tab-size: 4;
    -o-tab-size: 4;
}

/* 링크 - 호버 효과와 애니메이션 */
.markdown-content a {
    color: var(--primary);
    text-decoration: none;
    position: relative;
    font-weight: 500;
    transition: all 0.2s ease;
}

.markdown-content a::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--primary) 0%, color-mix(in oklch, var(--primary), transparent 40%) 100%);
    transition: width 0.3s ease;
}

.markdown-content a:hover {
    color: color-mix(in oklch, var(--primary), transparent 20%);
}

.markdown-content a:hover::after {
    width: 100%;
}

/* 테이블 - 모던한 그리드 스타일 */
.markdown-content table {
    width: 100%;
    margin: 1.5em 0;
    border-collapse: separate;
    border-spacing: 0;
    border-radius: 0.75rem;
    overflow: hidden;
    box-shadow: 0 4px 6px -1px color-mix(in oklch, var(--foreground), transparent 90%), 0 2px 4px -1px color-mix(in oklch, var(--foreground), transparent 94%);
    border: 1px solid color-mix(in oklch, var(--border), var(--foreground) 20%);
}

.markdown-content th,
.markdown-content td {
    padding: 0.875rem 1rem;
    text-align: left;
    border-bottom: 1px solid color-mix(in oklch, var(--border), var(--foreground) 30%);
}

.markdown-content th {
    background: color-mix(in oklch, var(--background), var(--foreground) 8%);
    font-weight: 650;
    color: var(--foreground);
    text-transform: uppercase;
    font-size: 0.8rem;
    letter-spacing: 0.05em;
}

.markdown-content td {
    background: color-mix(in oklch, var(--background), var(--foreground) 2%);
}

.markdown-content tr:hover td {
    background: color-mix(in oklch, var(--background), var(--foreground) 5%);
}

.markdown-content tr:last-child td {
    border-bottom: none;
}

/* 구분선 */
.markdown-content hr {
    margin: 2rem 0;
    border: none;
    height: 2px;
    background: linear-gradient(90deg, transparent 0%, hsl(var(--border)) 20%, hsl(var(--border)) 80%, transparent 100%);
}

/* 이미지 */
.markdown-content img {
    max-width: 100%;
    height: auto;
    border-radius: 0.5rem;
    box-shadow: 0 4px 6px -1px hsl(var(--foreground)/0.1);
    margin: 1rem 0;
}

/* 애니메이션 */
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.markdown-content {
    animation: fadeInUp 0.3s ease-out;
}

/* 다크 모드 최적화 */
.dark .markdown-content h1,
.dark .markdown-content h2,
.dark .markdown-content h3,
.dark .markdown-content h4,
.dark .markdown-content h5,
.dark .markdown-content h6 {
    background: linear-gradient(135deg, var(--primary) 0%, color-mix(in oklch, var(--primary), transparent 10%) 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

/* 다크 모드 구분선 */
.dark .markdown-content hr {
    background: linear-gradient(90deg, transparent 0%, hsl(var(--border) / 0.5) 20%, hsl(var(--border) / 0.5) 80%, transparent 100%);
}

/* 다크 모드 이미지 그림자 */
.dark .markdown-content img {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.5);
    opacity: 0.9;
}

/* 다크 모드 복사 버튼 */
.dark .markdown-content .copy-code-btn {
    background: hsl(var(--muted) / 0.3);
    border-color: hsl(var(--border) / 0.3);
    color: hsl(var(--muted-foreground));
}

.dark .markdown-content .copy-code-btn:hover {
    background: hsl(var(--muted) / 0.5);
    color: hsl(var(--foreground));
}

/* 다크 모드 스크롤바 */
.dark .markdown-content pre::-webkit-scrollbar-track {
    background: hsl(var(--muted) / 0.1);
}

.dark .markdown-content pre::-webkit-scrollbar-thumb {
    background: hsl(var(--muted-foreground) / 0.2);
}

.dark .markdown-content pre::-webkit-scrollbar-thumb:hover {
    background: hsl(var(--muted-foreground) / 0.4);
}

/* 모바일 최적화 */
@media (max-width: 768px) {
    .markdown-content {
        font-size: 0.9rem;
        line-height: 1.6;
    }

    .markdown-content h1 {
        font-size: 1.5rem;
    }

    .markdown-content h2 {
        font-size: 1.35rem;
    }

    .markdown-content h3 {
        font-size: 1.2rem;
    }

    .markdown-content h4 {
        font-size: 1.1rem;
    }

    .markdown-content pre {
        padding: 1rem;
        margin: 1rem 0;
        font-size: 0.8rem;
    }

    .markdown-content code {
        font-size: 0.8rem;
    }

    .markdown-content blockquote {
        padding: 1rem;
        margin: 1rem 0;
    }

    .markdown-content table {
        font-size: 0.85rem;
    }

    .markdown-content th,
    .markdown-content td {
        padding: 0.625rem 0.75rem;
    }
}

/* 스크롤바 스타일링 (기본) */
.markdown-content pre::-webkit-scrollbar {
    height: 8px;
}

.markdown-content pre::-webkit-scrollbar-track {
    background: hsl(var(--muted)/0.3);
    border-radius: 4px;
}

.markdown-content pre::-webkit-scrollbar-thumb {
    background: hsl(var(--muted-foreground)/0.3);
    border-radius: 4px;
}

.markdown-content pre::-webkit-scrollbar-thumb:hover {
    background: hsl(var(--muted-foreground)/0.5);
}
</style>
