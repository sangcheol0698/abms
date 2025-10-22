export type ChatRole = 'assistant' | 'user';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  createdAt: Date;
}

export interface ChatMessagePayload {
  id: string;
  role: ChatRole;
  content: string;
  createdAt?: string | Date | null;
}

export interface ChatRequest {
  sessionId?: string;
  content: string;
}

export interface ChatResponse {
  sessionId: string;
  messages: ChatMessagePayload[];
}

export function createChatMessage(role: ChatRole, content: string): ChatMessage {
  return {
    id: crypto.randomUUID(),
    role,
    content,
    createdAt: new Date(),
  };
}

export function normalizeChatMessage(payload: ChatMessagePayload): ChatMessage {
  const createdAt =
    payload.createdAt instanceof Date || payload.createdAt === undefined || payload.createdAt === null
      ? payload.createdAt
      : new Date(payload.createdAt);

  return {
    id: payload.id,
    role: payload.role,
    content: payload.content,
    createdAt: createdAt instanceof Date && !Number.isNaN(createdAt.getTime())
      ? createdAt
      : new Date(),
  };
}
