export async function copyTextToClipboard(text: string) {
  if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text);
      return;
    } catch {
      // Fallback to the legacy copy path below.
    }
  }

  if (typeof document === 'undefined') {
    throw new Error('Clipboard API is unavailable');
  }

  const activeElement = document.activeElement instanceof HTMLElement ? document.activeElement : null;
  const textarea = document.createElement('textarea');
  textarea.value = text;
  textarea.setAttribute('readonly', '');
  textarea.setAttribute('aria-hidden', 'true');
  textarea.style.position = 'fixed';
  textarea.style.top = '0';
  textarea.style.left = '0';
  textarea.style.opacity = '0';

  document.body.appendChild(textarea);
  textarea.focus();
  textarea.select();
  textarea.setSelectionRange(0, textarea.value.length);

  const copied = typeof document.execCommand === 'function'
    ? document.execCommand('copy')
    : false;
  document.body.removeChild(textarea);
  activeElement?.focus();

  if (!copied) {
    const promptWindow = typeof window !== 'undefined' ? window : null;
    if (promptWindow?.prompt) {
      promptWindow.prompt('자동 복사가 지원되지 않습니다. 아래 내용을 복사하세요.', text);
      return;
    }
    throw new Error('Failed to copy text');
  }
}
