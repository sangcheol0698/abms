import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import type {
  ChatRequest,
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
    _onError?: (error: Error) => void
  ): Promise<void> {
    const responseText = `\n현재는 샘플 응답만 제공하고 있어요.\n\n> "${request.content}"에 대한 정보는 곧 실제 서비스 데이터와 연동될 예정입니다.`;
    const chunks = responseText.split('');
    for (const chunk of chunks) {
      await new Promise((resolve) => setTimeout(resolve, 20));
      onChunk(chunk);
    }
  }
}


