import type { ChatRepository } from './ChatRepository';
import type { ChatRequest, ChatResponse } from '../entity/ChatMessage';

export class RemoteChatRepository implements ChatRepository {
    async sendMessage(request: ChatRequest): Promise<ChatResponse> {
        throw new Error('Method not implemented. Use streamMessage.');
    }

    async *streamMessage(request: ChatRequest): AsyncGenerator<string, void, unknown> {
        const response = await fetch('http://localhost:8080/api/v1/chat/stream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        if (!response.body) throw new Error('No response body');

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');

            // Keep the last line in buffer if it's incomplete
            buffer = lines.pop() || '';

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    const data = line.slice(5); // Don't trim immediately to preserve spaces if needed, but usually SSE data is trimmed.
                    // Spring AI might send raw text in data.
                    // Let's yield it.
                    yield data;
                }
            }
        }
    }
}
