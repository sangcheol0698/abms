import { describe, expect, it } from 'vitest';
import { normalizeNotificationType } from '@/features/notification/models/notification';
import { mapPartyDetail } from '@/features/party/models/partyDetail';
import { mapPartyListItem } from '@/features/party/models/partyListItem';

describe('party and notification models', () => {
  it('협력사 목록/상세 모델을 기본값과 함께 매핑한다', () => {
    expect(mapPartyListItem(undefined)).toEqual({
      partyId: 0,
      name: '',
      ceo: '',
      manager: '',
      contact: '',
      email: '',
    });

    expect(mapPartyDetail(undefined)).toEqual({
      partyId: 0,
      name: '',
      ceo: null,
      manager: null,
      contact: null,
      email: null,
    });

    expect(
      mapPartyDetail({
        partyId: 1,
        name: '협력사A',
        ceo: '대표',
        manager: '담당',
        contact: '010',
        email: 'a@abacus.co.kr',
      }),
    ).toEqual({
      partyId: 1,
      name: '협력사A',
      ceo: '대표',
      manager: '담당',
      contact: '010',
      email: 'a@abacus.co.kr',
    });
  });

  it('알림 타입을 대소문자와 null에 따라 정규화한다', () => {
    expect(normalizeNotificationType('SUCCESS')).toBe('success');
    expect(normalizeNotificationType('warning')).toBe('warning');
    expect(normalizeNotificationType('ERROR')).toBe('error');
    expect(normalizeNotificationType(null)).toBe('info');
    expect(normalizeNotificationType('UNKNOWN')).toBe('info');
  });
});
