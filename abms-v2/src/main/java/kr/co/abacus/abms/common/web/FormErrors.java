package kr.co.abacus.abms.common.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * 폼 검증 오류. 필드별 첫 번째 메시지와 전역 메시지를 담는다.
 */
public final class FormErrors {

    private static final FormErrors EMPTY = new FormErrors(Map.of(), null);

    private final Map<String, String> fieldErrors;
    private final @Nullable String globalError;

    private FormErrors(Map<String, String> fieldErrors, @Nullable String globalError) {
        this.fieldErrors = fieldErrors;
        this.globalError = globalError;
    }

    public static FormErrors none() {
        return EMPTY;
    }

    public static FormErrors of(BindingResult result) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : result.getFieldErrors()) {
            String message = error.isBindingFailure() ? "입력 형식이 올바르지 않습니다." : error.getDefaultMessage();
            fields.putIfAbsent(error.getField(), message == null ? "올바르지 않은 값입니다." : message);
        }
        String global = result.getGlobalErrors().isEmpty() ? null : result.getGlobalErrors().getFirst().getDefaultMessage();
        return new FormErrors(fields, global);
    }

    public static FormErrors global(String message) {
        return new FormErrors(Map.of(), message);
    }

    public boolean has(String field) {
        return fieldErrors.containsKey(field);
    }

    public @Nullable String get(String field) {
        return fieldErrors.get(field);
    }

    public @Nullable String global() {
        return globalError;
    }

    public boolean isEmpty() {
        return fieldErrors.isEmpty() && globalError == null;
    }

}
