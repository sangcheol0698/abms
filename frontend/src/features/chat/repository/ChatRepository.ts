import type { ChatRequest, ChatResponse } from '@/features/chat/entity/ChatMessage';

export interface ChatRepository {
  sendMessage(request: ChatRequest): Promise<ChatResponse>;
}
