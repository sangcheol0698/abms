import type { Router } from 'vue-router';
import { AUTH_HTTP_ERROR_EVENT, type AuthHttpErrorDetail } from '@/features/auth/http-auth-error';
import { clearStoredUser } from '@/features/auth/session';

let isRegistered = false;

function isOnAuthPage(path: string): boolean {
  return path.startsWith('/auths');
}

export function registerAuthHttpErrorHandler(router: Router): void {
  if (isRegistered || typeof window === 'undefined') {
    return;
  }

  window.addEventListener(AUTH_HTTP_ERROR_EVENT, (event: Event) => {
    const customEvent = event as CustomEvent<AuthHttpErrorDetail>;
    const status = customEvent.detail?.status;
    const route = router.currentRoute.value;

    if (status === 401) {
      clearStoredUser();
      if (route.name === 'auth-session-expired') {
        return;
      }
      void router.push({
        name: 'auth-session-expired',
        query: isOnAuthPage(route.path) ? undefined : { redirect: route.fullPath },
      });
      return;
    }

    if (status === 403 && route.name !== 'auth-forbidden') {
      void router.push({ name: 'auth-forbidden' });
    }
  });

  isRegistered = true;
}
