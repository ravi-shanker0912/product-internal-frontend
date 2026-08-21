import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { setOnRefreshFailed } from '../api/client';
import { authApi } from '../api/endpoints';

interface AuthContextValue {
  isAuthenticated: boolean;
  phone: string;
  login: (phone: string) => void;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [phone, setPhone] = useState('');

  useEffect(() => {
    // A refresh token rejected mid-session (revoked/expired) drops the user
    // back to login rather than leaving them stuck on a screen that will
    // just keep 401-ing.
    setOnRefreshFailed(() => setIsAuthenticated(false));
  }, []);

  const login = useCallback((loggedInPhone: string) => {
    setPhone(loggedInPhone);
    setIsAuthenticated(true);
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setIsAuthenticated(false);
  }, []);

  const value = useMemo(
    () => ({ isAuthenticated, phone, login, logout }),
    [isAuthenticated, phone, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
