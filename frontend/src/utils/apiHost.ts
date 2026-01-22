const LOOPBACK_HOSTS = new Set(['localhost', '127.0.0.1', '::1', '[::1]']);

function isLoopbackHostname(hostname: string) {
  return LOOPBACK_HOSTS.has(String(hostname || '').toLowerCase());
}

function isLoopbackUrl(value: string) {
  try {
    const parsed = new URL(value);
    return isLoopbackHostname(parsed.hostname);
  } catch {
    return false;
  }
}

function normalizeLoopbackHost(apiUrl: string, pageHost: string) {
  try {
    const parsed = new URL(apiUrl);
    if (isLoopbackHostname(pageHost) && parsed.hostname !== pageHost) {
      parsed.hostname = pageHost;
    }
    const base = parsed.pathname && parsed.pathname !== '/' ? `${parsed.origin}${parsed.pathname}` : parsed.origin;
    return `${base}${parsed.search}${parsed.hash}`;
  } catch {
    return apiUrl;
  }
}

export function resolveApiHost() {
  const env = import.meta.env.MODE || 'development';
  const isProxy = import.meta.env.VITE_IS_REQUEST_PROXY === 'true';
  const apiUrl = import.meta.env.VITE_API_URL;

  if (env === 'mock' || !isProxy || !apiUrl) return '';
  if (typeof window !== 'undefined') {
    const pageHost = window.location.hostname;
    if (isLoopbackUrl(apiUrl)) {
      if (!isLoopbackHostname(pageHost)) {
        // Avoid pointing external clients at their own localhost.
        return '';
      }
      return normalizeLoopbackHost(apiUrl, pageHost);
    }
  }
  return apiUrl;
}
