/**
 * Spring RestResponse veya düz DTO:
 * { data: { accessToken, refreshToken } } | { accessToken, refreshToken }
 */
export function parseAuthTokensFromResponseBody(restBody) {
  const payload = restBody?.data ?? restBody;
  if (!payload || typeof payload !== 'object') return null;
  const accessToken = payload.accessToken ?? payload.access_token;
  const refreshToken = payload.refreshToken ?? payload.refresh_token;
  if (!accessToken) return null;
  return { accessToken, refreshToken };
}
