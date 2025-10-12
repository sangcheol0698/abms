import type { AxiosError } from 'axios';

interface ApiErrorResponse {
  code?: string;
  message?: string;
  detail?: string;
  status?: string;
  path?: string;
}

/**
 * 공통 HTTP 오류 래퍼. Axios 오류와 서버 응답을 일관된 형태로 노출합니다.
 */
export default class HttpError extends Error {
  readonly code: string;
  readonly status?: number;
  readonly payload?: unknown;

  constructor(error: AxiosError | { code?: string; message: string }) {
    super('요청 처리 중 오류가 발생했습니다.');
    Object.setPrototypeOf(this, HttpError.prototype);

    const axiosError = error as AxiosError<ApiErrorResponse>;

    if (axiosError?.response) {
      const { status, data } = axiosError.response;
      this.message =
        data?.message || data?.detail || axiosError.message || '요청 처리 중 오류가 발생했습니다.';
      this.code = (data?.code as string) || String(status ?? '500');
      this.status = status ?? undefined;
      this.payload = data;
      return;
    }

    const fallback = error as { code?: string; message: string };
    this.message = fallback.message;
    this.code = fallback.code ?? 'CLIENT';
    this.payload = undefined;
  }

  isSessionExpired() {
    return this.code === '401';
  }
}
