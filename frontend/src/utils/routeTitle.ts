const PLACEHOLDER_TITLE_PATTERN = /^[\s?？._\-*/\\|]+$/u;

export function isPlaceholderTitle(value?: string | null): boolean {
  if (value == null) return true;
  const normalized = String(value).trim();
  if (!normalized) return true;
  return PLACEHOLDER_TITLE_PATTERN.test(normalized);
}

export function resolveRouteTitle(
  title: string | Record<string, string> | undefined,
  locale?: string,
  fallback = '',
): string {
  if (!title) return fallback;
  if (typeof title === 'string') {
    return isPlaceholderTitle(title) ? fallback : title;
  }

  const candidates: string[] = [];
  if (locale && title[locale]) candidates.push(title[locale]);
  if (title.zh_CN) candidates.push(title.zh_CN);
  if (title.en_US) candidates.push(title.en_US);
  Object.values(title).forEach((value) => {
    if (value != null) candidates.push(value);
  });

  for (const candidate of candidates) {
    if (!isPlaceholderTitle(candidate)) return candidate.trim();
  }

  return fallback;
}
