import { describe, expect, it, vi } from 'vitest';
import { createChatMessage } from './ChatMessage';

describe('ChatMessage', () => {
  it('crypto 없이도 채팅 메시지 id를 생성한다', () => {
    vi.spyOn(Date, 'now').mockReturnValue(1700000000000);

    const first = createChatMessage('user', 'hello');
    const second = createChatMessage('assistant', 'world');

    expect(first.id).toBe('chat-1700000000000-1');
    expect(second.id).toBe('chat-1700000000000-2');
    expect(first.id).not.toBe(second.id);
  });
});
