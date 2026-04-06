package kr.co.abacus.abms.application.weeklyreport;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;

@Component
class WeeklyReportSnapshotJsonMapper {

    private final ObjectMapper objectMapper;

    WeeklyReportSnapshotJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("주간 보고서 스냅샷 직렬화에 실패했습니다.", exception);
        }
    }

    WeeklyReportSnapshot fromJson(String json) {
        try {
            return objectMapper.readValue(json, WeeklyReportSnapshot.class);
        } catch (Exception exception) {
            throw new IllegalStateException("주간 보고서 스냅샷 역직렬화에 실패했습니다.", exception);
        }
    }
}
