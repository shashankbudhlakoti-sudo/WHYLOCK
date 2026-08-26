"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { api, ApiClientError } from "@/lib/api";

interface DecodedToken {
  sub?: string;
  role?: string;
  exp?: number;
}

interface AuthContextValue {
  token: string | null;
  username: string | null;
  role: string | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const TOKEN_KEY = "wl_token";
const USER_KEY = "wl_user";

function decodeToken(token: string): DecodedToken {
  try {
    const payload = token.split(".")[1];
    const json = decodeURIComponent(
      atob(payload.replace(/-/g, "+").replace(/_/g, "/"))
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return {};
  }
}

function getStoredAuth(): { token: string | null; username: string | null; role: string | null } {
  if (typeof window === "undefined") {
    return { token: null, username: null, role: null };
  }
  const storedToken = localStorage.getItem(TOKEN_KEY);
  const storedUser = localStorage.getItem(USER_KEY);
  if (!storedToken) {
    return { token: null, username: null, role: null };
  }
  const decoded = decodeToken(storedToken);
  if (decoded.exp && decoded.exp * 1000 < Date.now()) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    return { token: null, username: null, role: null };
  }
  return {
    token: storedToken,
    username: storedUser || decoded.sub || null,
    role: decoded.role || null,
  };
}

const EMPTY_AUTH = { token: null, username: null, role: null };

export function AuthProvider({ children }: { children: ReactNode }) {
  // Always start "logged out" on both server and client so the very first
  // render matches. Real auth state (read from localStorage) is hydrated
  // in an effect, after mount, which React allows to differ post-hydration.
  const [{ token, username, role }, setAuthState] = useState<{
    token: string | null;
    username: string | null;
    role: string | null;
  }>(EMPTY_AUTH);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional: hydrate auth from localStorage after mount only
    setAuthState(getStoredAuth());
  }, []);

  const login = useCallback(async (user: string, password: string) => {
    try {
      const res = await api.login(user, password);
      const decoded = decodeToken(res.token);
      localStorage.setItem(TOKEN_KEY, res.token);
      localStorage.setItem(USER_KEY, user);
      setAuthState({ token: res.token, username: user, role: decoded.role || null });
    } catch (err) {
      if (err instanceof ApiClientError) {
        throw new Error(err.message);
      }
      throw new Error("Unable to reach the authentication service.");
    }
  }, []);

  const register = useCallback(async (user: string, email: string, password: string) => {
    try {
      await api.register(user, email, password);
      // Auto sign-in right after a successful registration so the user lands
      // straight in an authenticated state — no separate manual login step.
      await login(user, password);
    } catch (err) {
      if (err instanceof ApiClientError) {
        throw new Error(err.message);
      }
      throw new Error("Unable to reach the registration service.");
    }
  }, [login]);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setAuthState({ token: null, username: null, role: null });
  }, []);

  const value = useMemo(
    () => ({
      token,
      username,
      role,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token, username, role, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}