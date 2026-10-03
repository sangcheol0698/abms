package kr.co.abacus.abms.summary;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param scheduleEnabled 매일 정기 재집계 실행 여부
 */
@ConfigurationProperties("abms.summary")
public record SummaryProperties(boolean scheduleEnabled) {
}
