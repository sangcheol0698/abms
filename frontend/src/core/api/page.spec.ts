import { describe, expect, it } from 'vitest';
import PageResponse from '@/core/api/page';

describe('PageResponse.fromPage', () => {
  it('기본 page 응답 구조를 변환한다', () => {
    const result = PageResponse.fromPage(
      {
        content: [{ id: 1 }],
        number: 1,
        size: 20,
        totalPages: 3,
        totalElements: 42,
      },
      (item) => ({ id: item.id }),
    );

    expect(result).toEqual({
      page: 2,
      size: 20,
      totalPages: 3,
      totalElements: 42,
      content: [{ id: 1 }],
    });
  });

  it('fallback 필드를 사용해 page 응답을 변환한다', () => {
    const result = PageResponse.fromPage(
      {
        content: [{ value: 'A' }, { value: 'B' }],
        page: {
          totalPages: 5,
          totalElements: 12,
        },
        pageable: {
          pageSize: 50,
        },
      },
      (item) => item.value,
    );

    expect(result).toEqual({
      page: 1,
      size: 50,
      totalPages: 5,
      totalElements: 12,
      content: ['A', 'B'],
    });
  });

  it('content가 없으면 빈 목록을 사용한다', () => {
    const result = PageResponse.fromPage(null, (item) => item);

    expect(result).toEqual({
      page: 1,
      size: 0,
      totalPages: 0,
      totalElements: 0,
      content: [],
    });
  });
});
