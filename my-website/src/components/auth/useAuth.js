import { toast } from "../common/toast.js";
import { useEffect, useRef, useState } from "react";
import { getCurrentUser, login, logout, refreshToken } from "../../api/auth/authApi.js";
import { accessTokenExpiresAt, getSession, refreshSessionTokens, saveSession, shouldRefreshAccessToken, waitForSessionRefresh } from "../../api/auth/authSession.js";
import { authErrorMessage, validateProfile, validateTokens } from "./authHelpers.js";

export function useAuth() {
  const [session, setSession] = useState(getSession);
  const [verification, setVerification] = useState(null);
  const [signingOut, setSigningOut] = useState(false);
  const logoutLock = useRef(false);
  const refreshFailure = useRef(null);
  const current = verification?.session === session ? verification : null;

  useEffect(() => {
    if (!session || verification?.session === session) return;
    const controller = new AbortController();
    const { signal } = controller;
    let active = getSession();
    if (!active || active.userId !== session.userId) return;
    async function restore() {
      try {
        const inFlightRefresh = waitForSessionRefresh();
        if (inFlightRefresh) {
          active = await inFlightRefresh;
          if (signal.aborted || !active || getSession() !== active || active.userId !== session.userId) return;
        }
        let profile;
        try { profile = await getCurrentUser({ signal }); }
        catch (error) {
          if (signal.aborted || getSession() !== active || error.response?.status !== 401) throw error;
          active = await refreshSessionTokens(refreshToken, active);
          if (signal.aborted || !active || getSession() !== active || active.userId !== session.userId) return;
          profile = await getCurrentUser({ signal });
        }
        if (signal.aborted || getSession() !== active) return;
        try { validateProfile(profile, active); }
        catch {
          saveSession(null); setSession(null); setVerification(null);
          toast.error("Tài khoản không hoạt động. Vui lòng đăng nhập lại.");
          return;
        }
        setSession(active);
        setVerification({ session: active, user: profile });
      } catch (error) {
        if (signal.aborted || getSession() !== active) return;
        if ([400, 401, 403, 404].includes(error.response?.status)) {
          saveSession(null); setSession(null); setVerification(null);
          toast.error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        } else { setSession(active); setVerification({ session: active, error: authErrorMessage(error, "Không xác minh được phiên đăng nhập.") }); }
      }
    }
    void restore();
    return () => controller.abort();
  }, [session, verification]);

  async function refreshForNavigation() {
    const active = getSession();
    if (!active || active !== session || !current?.user || logoutLock.current || !shouldRefreshAccessToken(active)) return;
    if (refreshFailure.current?.session === active && Date.now() - refreshFailure.current.at < 30_000) return;
    const profile = current.user;
    try {
      const refreshed = await refreshSessionTokens(refreshToken, active);
      if (!refreshed || getSession() !== refreshed || refreshed.userId !== active.userId) return;
      refreshFailure.current = null;
      // Keep the already-verified profile while rotating tokens: this avoids
      // remounting the account cart and protected page on a successful refresh.
      setSession(refreshed);
      setVerification({ session: refreshed, user: profile });
    } catch (failure) {
      if (getSession() !== active) return;
      if ([400, 401, 403].includes(failure.response?.status)) {
        saveSession(null); setSession(null); setVerification(null);
        toast.error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
      } else {
        refreshFailure.current = { session: active, at: Date.now() };
        const message = authErrorMessage(failure, "Chưa làm mới được phiên đăng nhập. Vui lòng thử lại.");
        if ((accessTokenExpiresAt(active.accessToken) ?? 0) <= Date.now()) setVerification({ session: active, error: message });
        toast.error(message);
      }
    }
  }

  async function signIn(input, options = {}) {
    const tokens = validateTokens(await login(input, options));
    if (options.signal?.aborted) return;
    const profile = await getCurrentUser({ ...options, headers: { Authorization: `Bearer ${tokens.accessToken}` } });
    if (options.signal?.aborted) return;
    validateProfile(profile, tokens);
    const saved = saveSession(tokens);
    setSession(saved);
    setVerification({ session: saved, user: profile });
    toast.success("Đăng nhập thành công.");
  }

  async function signOut() {
    if (logoutLock.current) return;
    const active = getSession();
    logoutLock.current = true;
    setSigningOut(true);
    // Clear the local session immediately, even if server revocation fails.
    saveSession(null); setSession(null); setVerification(null);
    try {
      if (active) await logout(active.refreshToken);
      toast.success("Đã đăng xuất.");
    } catch {
      toast.error("Đã đăng xuất trên thiết bị này. Chưa xác nhận thu hồi phiên trên máy chủ.");
    } finally { logoutLock.current = false; setSigningOut(false); }
  }

  return {
    user: current?.user ?? null, checking: Boolean(session && !current),
    sessionError: current?.error, signingOut, signIn, signOut, refreshForNavigation,
    retry: () => setVerification(null),
  };
}
