import { describe, it, expect, vi } from 'vitest';
import PageResponse from '../PageResponse';

describe('PageResponse.fromPage', () => {
  it('페이지 정보를 1부터 시작하도록 변환하고 콘텐츠를 매핑한다', () => {
    const apiResponse = {
      content: [
        { id: 1, label: 'A' },
        { id: 2, label: 'B' },
      ],
      number: 0,
      size: 5,
      totalPages: 4,
      totalElements: 20,
    };

    const result = PageResponse.fromPage(apiResponse, (item) => ({
      identifier: String(item.id),
      name: item.label,
    }));

    expect(result.page).toBe(1);
    expect(result.size).toBe(5);
    expect(result.totalPages).toBe(4);
    expect(result.totalElements).toBe(20);
    expect(result.content).toEqual([
      { identifier: '1', name: 'A' },
      { identifier: '2', name: 'B' },
    ]);
  });

  it('누락된 필드를 page/pageable 정보를 통해 보완한다', () => {
    const apiResponse = {
      content: [{ id: 'x', name: '조직' }],
      number: 3,
      pageable: { pageSize: 25 },
      page: { totalPages: 7, totalElements: 150 },
    };

    const result = PageResponse.fromPage(apiResponse, (item) => item.name);

    expect(result.page).toBe(4);
    expect(result.size).toBe(25);
    expect(result.totalPages).toBe(7);
    expect(result.totalElements).toBe(150);
    expect(result.content).toEqual(['조직']);
  });

  it('콘텐츠가 없으면 빈 배열을 반환하고 매퍼를 호출하지 않는다', () => {
    const apiResponse = {
      content: null,
      totalPages: undefined,
      totalElements: undefined,
      number: 0,
    } as const;

    const mapper = vi.fn();

    const result = PageResponse.fromPage(apiResponse, mapper);

    expect(result.content).toEqual([]);
    expect(result.size).toBe(0);
    expect(result.totalPages).toBe(0);
    expect(result.totalElements).toBe(0);
    expect(mapper).not.toHaveBeenCalled();
  });
});
