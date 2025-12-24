package kr.co.abacus.abms.application.chat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

import reactor.core.publisher.Flux;

/**
 * Advisor that detects and notifies about tool calls during chat interactions.
 */
public class ToolCallNotificationAdvisor implements StreamAdvisor, CallAdvisor {

    private final Consumer<String> toolCallNotifier;
    private final int order;

    public ToolCallNotificationAdvisor(Consumer<String> toolCallNotifier) {
        this(toolCallNotifier, Ordered.LOWEST_PRECEDENCE - 1);
    }

    public ToolCallNotificationAdvisor(Consumer<String> toolCallNotifier, int order) {
        this.toolCallNotifier = toolCallNotifier;
        this.order = order;
    }

    @Override
    public String getName() {
        return "ToolCallNotificationAdvisor";
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest chatClientRequest,
            CallAdvisorChain chain
    ) {
        ChatClientResponse response = chain.nextCall(chatClientRequest);

        // Check for tool calls in the response
        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse != null) {
            extractAndNotifyToolCalls(chatResponse);
        }

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(
            ChatClientRequest chatClientRequest,
            StreamAdvisorChain chain
    ) {
        List<String> notifiedTools = new CopyOnWriteArrayList<>();

        return chain.nextStream(chatClientRequest)
                .doOnNext(response -> {
                    ChatResponse chatResponse = response.chatResponse();
                    if (chatResponse != null) {
                        for (Generation generation : chatResponse.getResults()) {
                            if (generation.getOutput() != null &&
                                    generation.getOutput().getToolCalls() != null) {
                                for (var toolCall : generation.getOutput().getToolCalls()) {
                                    String toolName = toolCall.name();
                                    if (!notifiedTools.contains(toolName)) {
                                        notifiedTools.add(toolName);
                                        toolCallNotifier.accept(toolName);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void extractAndNotifyToolCalls(ChatResponse chatResponse) {
        for (Generation generation : chatResponse.getResults()) {
            if (generation.getOutput() != null &&
                    generation.getOutput().getToolCalls() != null) {
                for (var toolCall : generation.getOutput().getToolCalls()) {
                    toolCallNotifier.accept(toolCall.name());
                }
            }
        }
    }

}
