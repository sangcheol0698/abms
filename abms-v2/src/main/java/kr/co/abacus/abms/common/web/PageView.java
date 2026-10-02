package kr.co.abacus.abms.common.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 페이지네이션 표시용 모델.
 *
 * @param baseUrl 현재 검색 조건이 포함된 URL (page 파라미터는 교체된다)
 */
public record PageView<T>(List<T> content, int page, int totalPages, long totalElements, int size, String baseUrl) {

    public static <T> PageView<T> of(Page<T> page, String baseUrl) {
        return new PageView<>(page.getContent(), page.getNumber(), page.getTotalPages(), page.getTotalElements(),
                page.getSize(), baseUrl);
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page + 1 < totalPages;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public long firstIndex() {
        return totalElements == 0 ? 0 : (long) page * size + 1;
    }

    public long lastIndex() {
        return (long) page * size + content.size();
    }

    public String urlFor(int targetPage) {
        return UriComponentsBuilder.fromUriString(baseUrl).replaceQueryParam("page", targetPage).build().toUriString();
    }

    /** 현재 페이지 주변 최대 5개 페이지 번호 */
    public List<Integer> pageNumbers() {
        int start = Math.max(0, Math.min(page - 2, totalPages - 5));
        int end = Math.min(totalPages, start + 5);
        List<Integer> numbers = new ArrayList<>();
        for (int i = start; i < end; i++) {
            numbers.add(i);
        }
        return numbers;
    }

}
