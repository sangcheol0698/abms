package kr.co.abacus.abms.application.weeklyreport;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportInsightData;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;

@Service
class WeeklyReportAiWriter {

    private static final String SYSTEM_PROMPT = """
            당신은 ABMS 운영 총괄용 주간 보고서 작성 보조자입니다.
            제공된 JSON과 인사이트만 사용해서 운영 보고서 초안을 작성하세요.
            숫자, 상태, 일정, 이름은 입력에 없는 내용을 만들면 안 됩니다.
            """;

    private final ChatClient chatClient;
    private final WeeklyReportPromptFactory promptFactory;
    private final String modelName;

    WeeklyReportAiWriter(
            ChatClient.Builder chatClientBuilder,
            WeeklyReportPromptFactory promptFactory,
            @Value("${spring.ai.openai.chat.options.model:unknown}") String modelName
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.promptFactory = promptFactory;
        this.modelName = modelName;
    }

    String write(WeeklyReportSnapshot snapshot, WeeklyReportInsightData insightData) {
        String prompt = promptFactory.buildUserPrompt(snapshot, insightData);
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("주간 운영 보고서 AI 초안 생성에 실패했습니다.");
        }
        return content.trim();
    }

    String modelName() {
        return modelName;
    }
}
