export type ChatRole = 'assistant' | 'user';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  createdAt: Date;
}

export interface ChatRequest {
  sessionId?: string;
  content: string;
}

export interface ChatResponse {
  sessionId: string;
  messages: ChatMessage[];
}

export function createChatMessage(role: ChatRole, content: string): ChatMessage {
  return {
    id: crypto.randomUUID(),
    role,
    content,
    createdAt: new Date(),
  };
}
