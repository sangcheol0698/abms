import type { ChatRepository } from './ChatRepository';
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
  ): Promise<string | null> {
    const ctrl = new AbortController();

    return new Promise((resolve, reject) => {
      fetchEventSource(`${this.baseUrl}/api/v1/chat/stream`, {
        method: 'POST',
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
          reject(error);
        },
        onmessage(ev) {
          try {
            const data = JSON.parse(ev.data);
            if (data.type === 'text' && data.text != null) {
              onChunk(data.text);
            } else if (data.type === 'tool_call' && data.toolName != null && onToolCall) {
              onToolCall(data.toolName);
            }
          } catch {
            onChunk(ev.data);
          }
        },
        onclose() {
          // TODO: Return sessionId from response headers if new session was created
          resolve(null);
        },
        onerror(err) {
          const error = err instanceof Error ? err : new Error(String(err));
          if (onError) {
            onError(error);
          }
          ctrl.abort();
          reject(error);
        },
      });
    });
  }

  async getRecentSessions(limit = 20): Promise<ChatSession[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions?limit=${limit}`);

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data: ChatSessionResponse[] = await response.json();
    return data.map(normalizeChatSession);
  }

  async getFavoriteSessions(): Promise<ChatSession[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/favorites`);

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data: ChatSessionResponse[] = await response.json();
    return data.map(normalizeChatSession);
  }

  async getSessionDetail(sessionId: string): Promise<ChatSessionDetail> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/${sessionId}`);

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }

    const data: ChatSessionResponse = await response.json();
    return normalizeChatSessionDetail(data);
  }

  async toggleFavorite(sessionId: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/v1/chat/sessions/${sessionId}/favorite`, {
      method: 'POST',
    });

    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }
  }
}
