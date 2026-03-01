import type { ChatRepository, ChatStreamOptions } from './ChatRepository';
import type {
  ChatRequest,
  ChatSession,
  ChatSessionDetail,
  ChatSessionResponse,
} from '../entity/ChatMessage';
import { normalizeChatSession, normalizeChatSessionDetail } from '../entity/ChatMessage';
import { fetchEventSource } from '@microsoft/fetch-event-source';

export class RemoteChatRepository implements ChatRepository {
  private baseUrl = import.meta.env.VITE_API_BASE_URL || '';

  async sendMessage(request: ChatRequest): Promise<string> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/message`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data = await response.json();
    return data.content;
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
        },
        body: JSON.stringify(request),
        signal: ctrl.signal,
        async onopen(response) {
          if (response.ok && response.headers.get('content-type')?.includes('text/event-stream')) {
            return;
          }

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

          const error = new Error(errorMessage);
          if (onError) {
            onError(error);
          }
          ctrl.abort();
          rejectOnce(error);
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
          ctrl.abort();
          rejectOnce(error);
        },
      }).catch((err: unknown) => {
        if (abortedByCaller || isAbortError(err)) {
          resolveOnce();
          return;
        }
        const error = err instanceof Error ? err : new Error(String(err));
        if (onError) {
          onError(error);
        }
        rejectOnce(error);
      });
    });
  }

  async getRecentSessions(limit = 20): Promise<ChatSession[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions?limit=${limit}`, {
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data: ChatSessionResponse[] = await response.json();
    return data.map(normalizeChatSession);
  }

  async getFavoriteSessions(): Promise<ChatSession[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/favorites`, {
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data: ChatSessionResponse[] = await response.json();
    return data.map(normalizeChatSession);
  }

  async getSessionDetail(sessionId: string): Promise<ChatSessionDetail> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/${sessionId}`, {
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data: ChatSessionResponse = await response.json();
    return normalizeChatSessionDetail(data);
  }

  async toggleFavorite(sessionId: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/${sessionId}/favorite`, {
      method: 'POST',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }
  }

  async updateSessionTitle(sessionId: string, title: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/${sessionId}/title`, {
      method: 'PATCH',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ title }),
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }
  }

  async deleteSession(sessionId: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/${sessionId}`, {
      method: 'DELETE',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }
  }
}
