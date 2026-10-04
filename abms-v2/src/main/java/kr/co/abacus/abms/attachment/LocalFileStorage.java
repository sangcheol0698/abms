package kr.co.abacus.abms.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 로컬 디스크 저장소: {root}/{yyyy}/{MM}/{uuid}.{ext}. 원래 파일명은 DB 에만 두고 디스크에는 임의 이름으로 저장한다.
 */
@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(@Value("${abms.storage.path:./data/attachments}") String root) {
        // 환경 변수를 빈 값으로 두면 작업 디렉터리 전체가 저장소가 되지 않도록 기본 경로를 쓴다.
        this.root = Path.of(root.isBlank() ? "./data/attachments" : root).toAbsolutePath().normalize();
    }

    @Override
    public String store(InputStream content, String extension) throws IOException {
        YearMonth month = YearMonth.now();
        String relative = "%d/%02d/%s%s".formatted(month.getYear(), month.getMonthValue(), UUID.randomUUID(),
                extension.isEmpty() ? "" : "." + extension);
        Path target = resolve(relative);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        return relative;
    }

    @Override
    public InputStream open(String storedPath) throws IOException {
        return Files.newInputStream(resolve(storedPath));
    }

    @Override
    public void delete(String storedPath) throws IOException {
        Files.deleteIfExists(resolve(storedPath));
    }

    /** 저장소 밖 경로(../ 등)는 거부한다. */
    private Path resolve(String relative) {
        Path path = root.resolve(relative).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        return path;
    }

}
