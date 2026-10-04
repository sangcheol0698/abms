package kr.co.abacus.abms.attachment;

import java.io.IOException;
import java.io.InputStream;

/**
 * 첨부 파일 본문 저장소. 기본은 로컬 디스크({@link LocalFileStorage}), 필요하면 S3 등으로 바꾼다.
 */
public interface FileStorage {

    /** 저장하고 저장소 안의 상대 경로를 돌려준다. */
    String store(InputStream content, String extension) throws IOException;

    InputStream open(String storedPath) throws IOException;

    void delete(String storedPath) throws IOException;

}
