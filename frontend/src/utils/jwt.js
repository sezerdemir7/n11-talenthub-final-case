export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    return decoded;
  } catch {
    return null;
  }
}

export function isTokenExpired(token) {
  const decoded = decodeJwt(token);
  if (!decoded?.exp) return true;
  return decoded.exp * 1000 < Date.now();
}

export function getUserFromToken(token) {
  const decoded = decodeJwt(token);
  if (!decoded) return null;

  return {
    userId: decoded.userId,
    email: decoded.sub,
    roles: decoded.roles || [],
  };
}

export function hasRole(user, role) {
  if (!user?.roles) return false;
  return user.roles.includes(role) || user.roles.includes(`ROLE_${role}`);
}
