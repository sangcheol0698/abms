export function getCurrentYearMonth(date = new Date()): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}${month}`;
}

export function getCurrentYear(date = new Date()): number {
  return date.getFullYear();
}

export function formatAxisAmount(value: number): string {
  const absoluteValue = Math.abs(value);
  const sign = value < 0 ? '-' : '';

  if (absoluteValue >= 100000000) {
    const eokValue = absoluteValue / 100000000;
    const formatted = Number.isInteger(eokValue) ? `${eokValue}` : eokValue.toFixed(1);
    return `${sign}${formatted}억`;
  }

  if (absoluteValue >= 10000) {
    return `${sign}${Math.round(absoluteValue / 10000).toLocaleString()}만`;
  }

  return `${sign}${absoluteValue.toLocaleString()}`;
}

export function formatCurrencyAmount(value: number): string {
  return `${value.toLocaleString()}원`;
}

export function formatRelativeTimestamp(isoString: string): string {
  const created = new Date(isoString);
  const now = new Date();
  const diffMs = now.getTime() - created.getTime();

  if (diffMs < 0) {
    return '방금 전';
  }

  const diffMinutes = Math.floor(diffMs / 60000);
  if (diffMinutes < 1) return '방금 전';
  if (diffMinutes < 60) return `${diffMinutes}분 전`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}시간 전`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}일 전`;

  const y = created.getFullYear();
  const m = String(created.getMonth() + 1).padStart(2, '0');
  const d = String(created.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
