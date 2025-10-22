import { inject, provide } from 'vue';
import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import { MockChatRepository } from '@/features/chat/repository/ChatRepository.mock';

const CHAT_REPOSITORY_KEY = Symbol('chatRepository');
const fallbackRepository: ChatRepository = new MockChatRepository();

export function provideChatRepository(repository: ChatRepository) {
  provide(CHAT_REPOSITORY_KEY, repository);
}

export function useChatRepository(): ChatRepository {
  return inject(CHAT_REPOSITORY_KEY, fallbackRepository);
}
