package kr.co.abacus.abms.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * 브라우저 기본 대화상자(alert·confirm·prompt, htmx 의 hx-prompt)를 쓰지 않는다.
 * 확인은 hx-confirm(→ confirm.js 확인창), 입력은 화면 안의 입력칸으로 받는다.
 */
class NativeDialogTest {

    private static final Pattern NATIVE = Pattern.compile("hx-prompt|(?<![\\w.])(?:window\\.)?(?:alert|confirm|prompt)\\s*\\(");

    @Test
    void 템플릿과_스크립트에_브라우저_기본_대화상자가_없다() throws IOException {
        List<String> found;
        try (Stream<Path> files = Stream.concat(Files.walk(Path.of("src/main/jte")), Files.walk(Path.of("src/main/resources/static/js")))) {
            found = files.filter(Files::isRegularFile)
                    .flatMap(NativeDialogTest::matches)
                    .toList();
        }
        assertThat(found).isEmpty();
    }

    private static Stream<String> matches(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            return java.util.stream.IntStream.range(0, lines.size())
                    .filter(i -> NATIVE.matcher(lines.get(i)).find())
                    .mapToObj(i -> file + ":" + (i + 1) + "  " + lines.get(i).strip());
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

}
