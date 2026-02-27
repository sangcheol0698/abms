export const AUTH_HTTP_ERROR_EVENT = 'abms:auth-http-error';

export interface AuthHttpErrorDetail {
  status: 401 | 403;
}

export function emitAuthHttpError(detail: AuthHttpErrorDetail): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.dispatchEvent(new CustomEvent<AuthHttpErrorDetail>(AUTH_HTTP_ERROR_EVENT, { detail }));
}
