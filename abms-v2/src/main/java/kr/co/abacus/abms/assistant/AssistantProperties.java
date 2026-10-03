package kr.co.abacus.abms.assistant;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 어시스턴트 설정. API 키가 없으면 어시스턴트/AI 보고서는 비활성화되고 대체 동작을 한다.
 *
 * @param apiKey OpenAI API 키 (spring.ai.openai.api-key 와 동일 값)
 */
@ConfigurationProperties("abms.ai")
public record AssistantProperties(@Nullable String apiKey) {

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("not-configured");
    }

}
