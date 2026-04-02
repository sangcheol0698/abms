package kr.co.abacus.abms.adapter.integration.observability;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("openAiConnectivity")
public class OpenAiConnectivityHealthIndicator implements HealthIndicator {

    private final String apiKey;
    private final boolean enabled;
    private final long timeoutMs;

    public OpenAiConnectivityHealthIndicator(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${app.observability.health.openai.enabled:true}") boolean enabled,
            @Value("${app.observability.health.openai.timeout-ms:2000}") long timeoutMs
    ) {
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Health health() {
        if (!enabled) {
            return Health.up().withDetail("status", "disabled").build();
        }
        if (apiKey == null || apiKey.isBlank()) {
            return Health.down().withDetail("reason", "missing_api_key").build();
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/models?limit=1"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Health.up().withDetail("statusCode", response.statusCode()).build();
            }
            return Health.down().withDetail("statusCode", response.statusCode()).build();
        } catch (Exception exception) {
            return Health.down(exception).build();
        }
    }
}
