import { inject, singleton } from 'tsyringe';
import type { ChatRepository, ChatStreamOptions } from './ChatRepository';
import type {
  ChatRequest,
  ChatSession,
  ChatSessionDetail,
  ChatSessionResponse,
} from '../entity/ChatMessage';
import { normalizeChatSession, normalizeChatSessionDetail } from '../entity/ChatMessage';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { emitAuthHttpError } from '@/features/auth/http-auth-error';
import HttpRepository from '@/core/http/HttpRepository';

@singleton()
export class RemoteChatRepository implements ChatRepository {
  private readonly baseUrl = import.meta.env.VITE_API_BASE_URL || '';

  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  private notifyAuthorizationError(status: number): void {
    if (status === 401 || status === 403) {
      emitAuthHttpError({ status });
    }
  }

  private getXsrfToken(): string | null {
    if (typeof document === 'undefined') {
      return null;
    }

    const tokenPair = document.cookie
      .split('; ')
      .find((cookie) => cookie.startsWith('XSRF-TOKEN='));

    if (!tokenPair) {
      return null;
    }

    const encodedToken = tokenPair.slice('XSRF-TOKEN='.length);
    if (!encodedToken) {
      return null;
    }

    try {
      return decodeURIComponent(encodedToken);
    } catch {
      return encodedToken;
    }
  }

  async sendMessage(request: ChatRequest): Promise<string> {
    const response = await this.httpRepository.post<{ content: string }>({
      path: '/api/v1/chat/message',
      data: request,
    });
    return response.content;
  }

  async streamMessage(
    request: ChatRequest,
    onChunk: (chunk: string) => void,
    onError?: (error: Error) => void,
    onToolCall?: (toolName: string) => void,
    options?: ChatStreamOptions,
  ): Promise<string | null> {
    const ctrl = new AbortController();
    const externalSignal = options?.signal;
    const notifyAuthorizationError = this.notifyAuthorizationError.bind(this);

    return new Promise((resolve, reject) => {
      let streamedSessionId: string | null = null;
      let settled = false;
      let abortedByCaller = false;
      let abortByCallerHandler: (() => void) | null = null;

      const cleanup = () => {
        if (externalSignal && abortByCallerHandler) {
          externalSignal.removeEventListener('abort', abortByCallerHandler);
        }
      };
      const resolveOnce = () => {
        if (settled) return;
        settled = true;
        cleanup();
        resolve(streamedSessionId);
      };
      const rejectOnce = (error: Error) => {
        if (settled) return;
        settled = true;
        cleanup();
        reject(error);
      };
      const isAbortError = (error: unknown): boolean =>
        error instanceof Error &&
          (error.name === 'AbortError' || error.message.toLowerCase().includes('aborted'));

      abortByCallerHandler = () => {
        abortedByCaller = true;
        ctrl.abort();
        resolveOnce();
      };
      if (externalSignal) {
        if (externalSignal.aborted) {
          abortByCallerHandler();
          return;
        }
        externalSignal.addEventListener('abort', abortByCallerHandler, { once: true });
      }

      void fetchEventSource(`${this.baseUrl}/api/v1/chat/stream`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          ...(this.getXsrfToken() ? { 'X-XSRF-TOKEN': this.getXsrfToken() as string } : {}),
        },
        body: JSON.stringify(request),
        signal: ctrl.signal,
        async onopen(response) {
          if (response.ok && response.headers.get('content-type')?.includes('text/event-stream')) {
            return;
          }

          notifyAuthorizationError(response.status);

          let errorMessage = `Server responded with status ${response.status}`;
          try {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
              const errorData = await response.json();
              errorMessage = errorData.detail || errorData.message || JSON.stringify(errorData);
            } else {
              errorMessage = await response.text();
            }
          } catch {
            // failed to parse error body, use default message
          }

          // Throw to force fetch-event-source to stop retrying via onerror handler.
          throw new Error(errorMessage);
        },
        onmessage(ev) {
          try {
            const data = JSON.parse(ev.data);
            if (data.type === 'text' && data.text != null) {
              onChunk(data.text);
            } else if (data.type === 'session' && data.sessionId != null) {
              streamedSessionId = data.sessionId;
            } else if (data.type === 'tool_call' && data.toolName != null && onToolCall) {
              onToolCall(data.toolName);
            }
          } catch {
            onChunk(ev.data);
          }
        },
        onclose() {
          resolveOnce();
        },
        onerror(err) {
          if (abortedByCaller || isAbortError(err)) {
            resolveOnce();
            return;
          }
          const error = err instanceof Error ? err : new Error(String(err));
          if (onError) {
            onError(error);
          }
          rejectOnce(error);
          // Important: throw to break fetch-event-source auto-retry loop.
          throw error;
        },
      }).catch((err: unknown) => {
        if (abortedByCaller || isAbortError(err)) {
          resolveOnce();
          return;
        }
        const error = err instanceof Error ? err : new Error(String(err));
        if (!settled && onError) {
          onError(error);
        }
        rejectOnce(error);
      });
    });
  }

  async getRecentSessions(limit = 20): Promise<ChatSession[]> {
    const data = await this.httpRepository.get<ChatSessionResponse[]>({
      path: '/api/v1/chat/sessions',
      params: { limit },
    });
    return data.map(normalizeChatSession);
  }

  async getFavoriteSessions(): Promise<ChatSession[]> {
    const data = await this.httpRepository.get<ChatSessionResponse[]>({
      path: '/api/v1/chat/sessions/favorites',
    });
    return data.map(normalizeChatSession);
  }

  async getSessionDetail(sessionId: string): Promise<ChatSessionDetail> {
    const data = await this.httpRepository.get<ChatSessionResponse>({
      path: `/api/v1/chat/sessions/${sessionId}`,
    });
    return normalizeChatSessionDetail(data);
  }

  async toggleFavorite(sessionId: string): Promise<void> {
    await this.httpRepository.post<void>({
      path: `/api/v1/chat/sessions/${sessionId}/favorite`,
    });
  }

  async updateSessionTitle(sessionId: string, title: string): Promise<void> {
    await this.httpRepository.patch<void>({
      path: `/api/v1/chat/sessions/${sessionId}/title`,
      data: { title },
    });
  }

  async deleteSession(sessionId: string): Promise<void> {
    await this.httpRepository.delete<void>({
      path: `/api/v1/chat/sessions/${sessionId}`,
    });
  }
}
