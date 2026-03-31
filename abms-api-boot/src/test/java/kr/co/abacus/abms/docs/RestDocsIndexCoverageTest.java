package kr.co.abacus.abms.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("REST Docs 인덱스 커버리지")
class RestDocsIndexCoverageTest {

    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("document\\(\"([^\"]+)\"");
    private static final Pattern INDEX_PATTERN = Pattern.compile("include::\\{snippets}/(.+?)/[^/]+\\.adoc\\[]");

    @Test
    @DisplayName("문서 스니펫 식별자는 index.adoc에 모두 포함되어야 한다")
    void everyDocumentIdentifierShouldBeIncludedInIndex() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        Path docsIndex = projectRoot.resolve("src/docs/asciidoc/index.adoc");
        Path apiTestRoot = projectRoot.resolve("src/test/java/kr/co/abacus/abms/adapter/api");

        Set<String> documentedIdentifiers = Files.walk(apiTestRoot)
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(this::extractDocumentIdentifiers)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> indexedIdentifiers = extractIndexIdentifiers(docsIndex);

        Set<String> missingIdentifiers = new TreeSet<>(documentedIdentifiers);
        missingIdentifiers.removeAll(indexedIdentifiers);

        assertThat(missingIdentifiers)
                .describedAs("index.adoc에 누락된 문서 식별자")
                .isEmpty();
    }

    private java.util.stream.Stream<String> extractDocumentIdentifiers(Path sourceFile) {
        try {
            String content = Files.readString(sourceFile);
            Matcher matcher = DOCUMENT_PATTERN.matcher(content);
            Set<String> identifiers = new TreeSet<>();
            while (matcher.find()) {
                identifiers.add(matcher.group(1));
            }
            return identifiers.stream();
        } catch (IOException exception) {
            throw new IllegalStateException("문서 식별자를 읽을 수 없습니다: " + sourceFile, exception);
        }
    }

    private Set<String> extractIndexIdentifiers(Path docsIndex) throws IOException {
        String content = Files.readString(docsIndex);
        Matcher matcher = INDEX_PATTERN.matcher(content);
        Set<String> identifiers = new TreeSet<>();
        while (matcher.find()) {
            identifiers.add(matcher.group(1));
        }
        return identifiers;
    }

}
