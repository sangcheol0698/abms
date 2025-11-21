import type { ChatRepository } from './ChatRepository';
import type { ChatRequest } from '../entity/ChatMessage';
import { fetchEventSource } from '@microsoft/fetch-event-source';

export class RemoteChatRepository implements ChatRepository {
    async sendMessage(request: ChatRequest): Promise<string> {
        const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
        const response = await fetch(`${baseUrl}/api/v1/chat/message`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            throw new Error(`Server responded with status ${response.status}`);
        }

        const data = await response.json();
        return data.content;
    }

    async streamMessage(
        request: ChatRequest,
        onChunk: (chunk: string) => void,
        onError?: (error: Error) => void
    ): Promise<void> {
        const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
        const ctrl = new AbortController();

        return new Promise((resolve, reject) => {
            fetchEventSource(`${baseUrl}/api/v1/chat/stream`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(request),
                signal: ctrl.signal,
                async onopen(response) {
                    if (response.ok && response.headers.get('content-type')?.includes('text/event-stream')) {
                        return; // everything is good
                    }

                    let errorMessage = `Server responded with status ${response.status}`;
                    try {
                        const contentType = response.headers.get('content-type');
                        if (contentType && contentType.includes('application/json')) {
                            const errorData = await response.json();
                            errorMessage = errorData.detail || errorData.message || JSON.stringify(errorData);
                        } else {
                            errorMessage = await response.text();
                        }
                    } catch (e) {
                        // failed to parse error body, use default message
                    }

                    const error = new Error(errorMessage);
                    if (onError) {
                        onError(error);
                    }
                    reject(error);
                },
                onmessage(ev) {
                    console.log('📦 Chunk received:', ev.data); // 이 로그가 나오는지 확인
                    onChunk(ev.data);
                },
                onclose() {
                    resolve();
                },
                onerror(err) {
                    const error = err instanceof Error ? err : new Error(String(err));
                    if (onError) {
                        onError(error);
                    }
                    ctrl.abort();
                    reject(error);
                },
            });
        });
    }
}
