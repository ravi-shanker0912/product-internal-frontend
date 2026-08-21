import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { tokenStore } from '../storage/tokenStore';
import { ApiError, RefreshBody, TokenPair } from './types';

/**
 * Points at the host machine's LAN IP rather than the emulator's 10.0.2.2 alias:
 * this Windows dev machine has a Hyper-V Firewall rule on the WSL virtual switch
 * that blocks TCP to 10.0.2.2, but allows it over Wi-Fi where docker's port
 * binding already has an existing allow rule. Backend runs on port 8080 (see
 * D:\Product Internal\driverapp docker-compose.yml). Update this if the host's
 * LAN IP changes (check with `ipconfig`).
 */
const BASE_URL = 'http://192.168.1.12:8080/';

/** Fired when the refresh token itself is rejected — caller should log the user out. */
let onRefreshFailed: (() => void) | null = null;
export function setOnRefreshFailed(cb: () => void): void {
  onRefreshFailed = cb;
}

// Bare client with no interceptors — used only for /api/auth/refresh, to avoid recursing.
const plainClient = axios.create({ baseURL: BASE_URL, timeout: 15000 });

export const apiClient: AxiosInstance = axios.create({ baseURL: BASE_URL, timeout: 15000 });

apiClient.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const token = await tokenStore.getAccessToken();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

/**
 * The backend rotates the refresh token on every use and treats replay of an
 * already-consumed one as a compromise signal — it revokes every session for
 * that user (see AuthService.refresh). Two requests 401-ing around the same
 * moment must not both call /api/auth/refresh with the same stale token, or
 * the loser's call reads as a replay and silently logs the user out
 * everywhere. This promise makes refresh single-flight: whoever triggers it
 * first refreshes; everyone else 401-ing while that's in flight just awaits
 * the same promise and reuses its result instead of refreshing again.
 */
let refreshPromise: Promise<TokenPair | null> | null = null;

async function doRefresh(): Promise<TokenPair | null> {
  const refreshToken = await tokenStore.getRefreshToken();
  if (!refreshToken) return null;
  try {
    const body: RefreshBody = { refreshToken };
    const { data } = await plainClient.post<TokenPair>('api/auth/refresh', body);
    await tokenStore.saveTokens(data.accessToken, data.refreshToken);
    return data;
  } catch {
    return null;
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    if (error.response?.status !== 401 || !original || original._retry) {
      return Promise.reject(error);
    }
    original._retry = true;

    if (!refreshPromise) {
      refreshPromise = doRefresh().finally(() => {
        refreshPromise = null;
      });
    }
    const newTokens = await refreshPromise;

    if (!newTokens) {
      onRefreshFailed?.();
      return Promise.reject(error);
    }

    original.headers.set('Authorization', `Bearer ${newTokens.accessToken}`);
    return apiClient.request(original);
  }
);

/** Turns a failed API call into the message the user should see. */
export function extractErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    if (err.response) {
      const apiError = err.response.data as ApiError | undefined;
      return apiError?.message ?? `Something went wrong (HTTP ${err.response.status})`;
    }
    if (err.request) {
      return "Can't reach the server. Check your connection and try again.";
    }
  }
  return err instanceof Error ? err.message : 'Something went wrong';
}
