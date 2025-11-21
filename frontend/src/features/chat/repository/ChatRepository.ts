import type { ChatRequest } from '@/features/chat/entity/ChatMessage';

export interface ChatRepository {
  sendMessage(request: ChatRequest): Promise<string>;
  streamMessage(
    request: ChatRequest,
    onChunk: (chunk: string) => void,
    onError?: (error: Error) => void
  ): Promise<void>;
}
