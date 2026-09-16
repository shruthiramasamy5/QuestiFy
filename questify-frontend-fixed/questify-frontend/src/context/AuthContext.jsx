import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import authService from "../services/authService.js";
import { setAuthTokenProvider } from "../services/api.js";
import { isKnownRole, ROLES } from "../utils/roles.js";

const AuthContext = createContext(null);
const SESSION_KEY = "questify.session";

function readStoredSession() {
  try {
    const raw = window.localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    return parsed && isKnownRole(parsed.user?.role) ? parsed : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => readStoredSession());
  const [initializing, setInitializing] = useState(false);
  const tokenRef = useRef(session?.token || null);

  // HOD-only UI toggle: lets an HOD view the Faculty workspace nav/pages
  // without changing their actual role, token, or making any auth call.
  const [actingAsFaculty, setActingAsFaculty] = useState(false);

  useEffect(() => {
    setAuthTokenProvider(() => tokenRef.current);
  }, []);

  const persist = useCallback((next) => {
    tokenRef.current = next?.token || null;
    setSession(next);
    setActingAsFaculty(false);
    try {
      if (next) window.localStorage.setItem(SESSION_KEY, JSON.stringify(next));
      else window.localStorage.removeItem(SESSION_KEY);
    } catch {
      /* storage unavailable - session stays in memory only */
    }
  }, []);

  const toggleActingAsFaculty = useCallback(() => {
    setActingAsFaculty((prev) => {
      if (session?.user?.role !== ROLES.HOD) return false;
      return !prev;
    });
  }, [session]);

  const login = useCallback(
    async (credentials) => {
      const response = await authService.login(credentials);
      const next = { user: response.user ?? response, token: response.token ?? null };
      persist(next);
      return next.user;
    },
    [persist]
  );

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      /* clearing the local session is what matters for the user */
    }
    persist(null);
  }, [persist]);

  const refreshUser = useCallback(async () => {
    setInitializing(true);
    try {
      const user = await authService.getCurrentUser();
      persist({ user, token: tokenRef.current });
      return user;
    } finally {
      setInitializing(false);
    }
  }, [persist]);

  const isActingAsFaculty = session?.user?.role === ROLES.HOD && actingAsFaculty;

  const value = useMemo(
    () => ({
      user: session?.user || null,
      isAuthenticated: Boolean(session?.user),
      initializing,
      login,
      logout,
      refreshUser,
      // Effective role drives nav/page display only — never used for API
      // calls or permission checks, which must always use `user.role`.
      actingAsFaculty: isActingAsFaculty,
      effectiveRole: isActingAsFaculty ? ROLES.FACULTY : session?.user?.role || null,
      toggleActingAsFaculty,
    }),
    [session, initializing, login, logout, refreshUser, isActingAsFaculty, toggleActingAsFaculty]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
