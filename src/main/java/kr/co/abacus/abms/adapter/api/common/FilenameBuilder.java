package kr.co.abacus.abms.adapter.api.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class FilenameBuilder {

    private static final String TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss";
    private static final String EXCEL_EXTENSION = ".xlsx";

    public static String build(String prefix) {
        String timestamp = LocalDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));

        return prefix + "_" + timestamp + EXCEL_EXTENSION;
    }

}
