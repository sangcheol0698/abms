import { toast } from 'vue-sonner';

let lastShownAt = 0;
const FORBIDDEN_TOAST_COOLDOWN_MS = 1500;

export function showForbiddenToast(): void {
  const now = Date.now();
  if (now - lastShownAt < FORBIDDEN_TOAST_COOLDOWN_MS) {
    return;
  }

  lastShownAt = now;
  toast.error('접근 권한이 없습니다.', {
    description: '필요한 권한이 없어 요청을 처리할 수 없습니다.',
  });
}
