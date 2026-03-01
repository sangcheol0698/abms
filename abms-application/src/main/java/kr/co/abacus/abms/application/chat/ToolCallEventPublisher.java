package kr.co.abacus.abms.application.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Request-scoped publisher for tool call events.
 * Tools can use this to emit events when they start executing.
 */
@Component
@RequestScope
public class ToolCallEventPublisher {

    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Emit a tool call event.
     *
     * @param toolName The name of the tool being called
     */
    public void publishToolCall(String toolName) {
        sink.tryEmitNext(toolName);
    }

    /**
     * Get the flux of tool call events.
     */
    public Flux<String> getToolCallEvents() {
        return sink.asFlux();
    }

    /**
     * Complete the sink when the request is done.
     */
    public void complete() {
        sink.tryEmitComplete();
    }

}
