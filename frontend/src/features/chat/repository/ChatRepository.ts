import type { ChatRequest, ChatSession, ChatSessionDetail } from '@/features/chat/entity/ChatMessage';

export interface ChatRepository {
  // Messages
  sendMessage(request: ChatRequest): Promise<string>;
  streamMessage(
    request: ChatRequest,
    onChunk: (chunk: string) => void,
    onError?: (error: Error) => void,
    onToolCall?: (toolName: string) => void
  ): Promise<string | null>; // Returns sessionId if new session created

  // Sessions
  getRecentSessions(limit?: number): Promise<ChatSession[]>;
  getFavoriteSessions(): Promise<ChatSession[]>;
  getSessionDetail(sessionId: string): Promise<ChatSessionDetail>;
  toggleFavorite(sessionId: string): Promise<void>;
  updateSessionTitle(sessionId: string, title: string): Promise<void>;
  deleteSession(sessionId: string): Promise<void>;
}
