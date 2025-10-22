import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import type {
  ChatMessagePayload,
  ChatRequest,
  ChatResponse,
} from '@/features/chat/entity/ChatMessage';

export class MockChatRepository implements ChatRepository {
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    const responseText = `\n현재는 샘플 응답만 제공하고 있어요.\n\n> “${request.content}”에 대한 정보는 곧 실제 서비스 데이터와 연동될 예정입니다.`;

    return {
      sessionId: request.sessionId ?? crypto.randomUUID(),
      messages: [
        createMockAssistantMessage(responseText),
      ],
    };
  }
}

function createMockAssistantMessage(content: string): ChatMessagePayload {
  return {
    id: crypto.randomUUID(),
    role: 'assistant',
    content,
    createdAt: new Date().toISOString(),
  };
}
