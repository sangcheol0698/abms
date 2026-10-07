package kr.co.abacus.abms.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * hx-trigger 의 from: 은 문서 전체에서 요소를 찾는다. 목록 필터가 from:input[name=q] 처럼 범위 없이 걸리면
 * 명령 팔레트(name=q)나 모달·달력의 select 를 건드릴 때도 목록이 다시 그려진다. 그래서 from: 은 폼 id 로 범위를 묶는다.
 */
class HtmxTriggerScopeTest {

    private static final Pattern TRIGGER = Pattern.compile("hx-trigger=\"([^\"]*)\"");
    private static final Pattern FROM = Pattern.compile("from:(\\S+)");

    @Test
    void hx_trigger_의_from_은_범위가_정해진_선택자만_쓴다() throws IOException {
        List<String> unscoped;
        try (Stream<Path> files = Files.walk(Path.of("src/main/jte"))) {
            unscoped = files.filter(p -> p.toString().endsWith(".jte")).flatMap(HtmxTriggerScopeTest::unscoped).toList();
        }
        assertThat(unscoped).isEmpty();
    }

    private static Stream<String> unscoped(Path file) {
        try {
            String content = Files.readString(file);
            Stream.Builder<String> found = Stream.builder();
            Matcher trigger = TRIGGER.matcher(content);
            while (trigger.find()) {
                Matcher from = FROM.matcher(trigger.group(1));
                while (from.find()) {
                    String selector = from.group(1);
                    // (#폼id …), body·document·window(사용자 정의 이벤트) 는 허용한다.
                    if (!selector.startsWith("(#") && !selector.matches("(body|document|window)[,]?")) {
                        found.add(file + "  from:" + selector);
                    }
                }
            }
            return found.build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
