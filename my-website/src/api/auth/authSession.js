const key = "market-lane-auth-session";

function validSession(value) {
  return ["userId", "accessToken", "refreshToken"].every(
    (field) => typeof value?.[field] === "string" && value[field].trim(),
  );
}

function restoreSession() {
  try {
    const saved = JSON.parse(globalThis.sessionStorage?.getItem(key) ?? "null");
    return validSession(saved) ? saved : null;
  } catch {
    return null;
  }
}

let session = restoreSession();

export function getSession() {
  return session;
}

export function saveSession(value) {
  if (value !== null && !validSession(value)) {
    throw new Error("Phản hồi đăng nhập không hợp lệ.");
  }
  session = value;
  try {
    if (value) globalThis.sessionStorage?.setItem(key, JSON.stringify(value));
    else globalThis.sessionStorage?.removeItem(key);
  } catch {
    // The current tab can continue with the in-memory session.
  }
  return session;
}


export const ACCESS_TOKEN_REFRESH_WINDOW_MS = 60_000;
let refreshTask = null;

// JWT decoding is only a scheduling hint; the backend still verifies the token.
export function accessTokenExpiresAt(accessToken) {
  try {
    const payload = accessToken.split(".")[1];
    if (!payload) return null;
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    const bytes = Uint8Array.from(globalThis.atob(padded), (character) => character.charCodeAt(0));
    const claims = JSON.parse(new TextDecoder().decode(bytes));
    return typeof claims.exp === "number" && Number.isFinite(claims.exp) && claims.exp > 0 ? claims.exp * 1000 : null;
  } catch { return null; }
}

export function shouldRefreshAccessToken(active, now = Date.now()) {
  const expiresAt = active && accessTokenExpiresAt(active.accessToken);
  return expiresAt !== null && Boolean(active) && expiresAt - now <= ACCESS_TOKEN_REFRESH_WINDOW_MS;
}

// A refresh token may rotate: keep one request alive across route changes and
// StrictMode cleanups, and never overwrite a later login/logout with its result.
export function refreshSessionTokens(refreshRequest, active = getSession()) {
  if (!active || getSession() !== active) return Promise.resolve(null);
  if (refreshTask?.active === active) return refreshTask.promise;
  const task = { active, promise: null };
  task.promise = Promise.resolve().then(() => {
    if (getSession() !== active) return null;
    return refreshRequest(active.refreshToken);
  }).then((tokens) => {
    if (getSession() !== active) return null;
    const next = { userId: active.userId, accessToken: tokens?.accessToken, refreshToken: tokens?.refreshToken };
    if (!validSession(next) || (tokens?.userId && tokens.userId !== active.userId)) throw new Error("Phản hồi làm mới phiên không hợp lệ.");
    return saveSession(next);
  }).finally(() => { if (refreshTask === task) refreshTask = null; });
  refreshTask = task;
  return task.promise;
}

export function waitForSessionRefresh() {
  return refreshTask?.active === getSession() ? refreshTask.promise : null;
}
