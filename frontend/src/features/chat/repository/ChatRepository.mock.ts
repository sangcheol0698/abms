import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import type { ChatStreamOptions } from '@/features/chat/repository/ChatRepository';
import type {
  ChatRequest,
  ChatSession,
  ChatSessionDetail,
} from '@/features/chat/entity/ChatMessage';

export class MockChatRepository implements ChatRepository {
  async sendMessage(request: ChatRequest): Promise<string> {
    const responseText = `\n현재는 샘플 응답만 제공하고 있어요.\n\n> “${request.content}”에 대한 정보는 곧 실제 서비스 데이터와 연동될 예정입니다.`;

    await new Promise((resolve) => setTimeout(resolve, 500));

    return responseText;
  }

  async streamMessage(
    request: ChatRequest,
    onChunk: (chunk: string) => void,
    onError?: (error: Error) => void,
    onToolCall?: (toolName: string) => void,
    options?: ChatStreamOptions
  ): Promise<string | null> {
    void onError;
    void onToolCall;
    void options;
    const responseText = `\n현재는 샘플 응답만 제공하고 있어요.\n\n> "${request.content}"에 대한 정보는 곧 실제 서비스 데이터와 연동될 예정입니다.`;
    const chunks = responseText.split('');
    for (const chunk of chunks) {
      await new Promise((resolve) => setTimeout(resolve, 20));
      onChunk(chunk);
    }
    return null;
  }

  async getRecentSessions(limit?: number): Promise<ChatSession[]> {
    void limit;
    return [];
  }

  async getFavoriteSessions(): Promise<ChatSession[]> {
    return [];
  }

  async getSessionDetail(sessionId: string): Promise<ChatSessionDetail> {
    return {
      id: 0,
      sessionId,
      title: '샘플 세션',
      messages: [],
      updatedAt: new Date(),
      createdAt: new Date(),
      favorite: false,
    };
  }

  async toggleFavorite(sessionId: string): Promise<void> {
    void sessionId;
    // Do nothing
  }

  async updateSessionTitle(sessionId: string, title: string): Promise<void> {
    void sessionId;
    void title;
    // Do nothing
  }

  async deleteSession(sessionId: string): Promise<void> {
    void sessionId;
    // Do nothing
  }
}
