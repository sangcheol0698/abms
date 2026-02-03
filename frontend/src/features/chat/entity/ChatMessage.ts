export type ChatRole = 'assistant' | 'user';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  toolStatus?: {
    name: string;
    emoji: string;
    description: string;
  };
  createdAt: Date;
}

export interface ChatMessagePayload {
  id: string;
  role: ChatRole;
  content: string;
  toolStatus?: {
    name: string;
    emoji: string;
    description: string;
  };
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

// Session types
export interface ChatSession {
  id: number;
  sessionId: string;
  title: string;
  favorite: boolean;
  updatedAt: Date;
}

export interface ChatSessionDetail extends ChatSession {
  messages: ChatMessage[];
  createdAt: Date;
}

// API Response types
export interface ChatSessionResponse {
  id: number;
  sessionId: string;
  title: string;
  favorite: boolean;
  messages: ChatMessagePayload[];
  createdAt: string;
  updatedAt: string;
}

export function createChatMessage(role: ChatRole, content: string): ChatMessage {
  return {
    id: crypto.randomUUID(),
    role,
    content,
    toolStatus: undefined,
    createdAt: new Date(),
  };
}

export function normalizeChatMessage(payload: ChatMessagePayload): ChatMessage {
  const createdAt =
    payload.createdAt instanceof Date || payload.createdAt === undefined || payload.createdAt === null
      ? payload.createdAt
      : new Date(payload.createdAt);

  return {
    id: String(payload.id),
    role: payload.role.toLowerCase() as ChatRole,
    content: payload.content,
    toolStatus: payload.toolStatus,
    createdAt: createdAt instanceof Date && !Number.isNaN(createdAt.getTime())
      ? createdAt
      : new Date(),
  };
}

export function normalizeChatSession(response: ChatSessionResponse): ChatSession {
  return {
    id: response.id,
    sessionId: response.sessionId,
    title: response.title,
    favorite: response.favorite,
    updatedAt: new Date(response.updatedAt),
  };
}

export function normalizeChatSessionDetail(response: ChatSessionResponse): ChatSessionDetail {
  return {
    id: response.id,
    sessionId: response.sessionId,
    title: response.title,
    favorite: response.favorite,
    messages: response.messages.map(normalizeChatMessage),
    createdAt: new Date(response.createdAt),
    updatedAt: new Date(response.updatedAt),
  };
}
