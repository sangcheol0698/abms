import { inject, provide } from 'vue';
import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import { RemoteChatRepository } from '@/features/chat/repository/RemoteChatRepository';

const CHAT_REPOSITORY_KEY = Symbol('chatRepository');
const fallbackRepository: ChatRepository = new RemoteChatRepository();

export function provideChatRepository(repository: ChatRepository) {
  provide(CHAT_REPOSITORY_KEY, repository);
}

export function useChatRepository(): ChatRepository {
  return inject(CHAT_REPOSITORY_KEY, fallbackRepository);
}
