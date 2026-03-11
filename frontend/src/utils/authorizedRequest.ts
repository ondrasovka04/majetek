/**
 * Pre-configured Axios instance with JWT authorization.
 * Attaches the bearer token from the auth store to every request.
 * On 401, automatically attempts a token refresh and retries the original request once.
 */
import axios, { AxiosHeaders, InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "@/store/authStore";
import { refresh } from "@/services/authService";

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const API_URL = import.meta.env.VITE_API_URL;

const authorizedRequest = axios.create({
  baseURL: API_URL,
});

authorizedRequest.interceptors.request.use(async (config) => {
  const { token, expiresIn, setAuth, clearAuth } = useAuthStore.getState();
  const now = new Date();

  if (token && expiresIn && new Date(expiresIn) <= now) {
    // Access expired: attempt refresh via HttpOnly cookie
    try {
      const refreshed = await refresh();
      setAuth(refreshed.user, refreshed.token, refreshed.expiresIn);
      config.headers.Authorization = `Bearer ${refreshed.token}`;
    } catch {
      clearAuth();
      throw new axios.Cancel("Token refresh failed – user logged out.");
    }
  } else if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

authorizedRequest.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as RetryableRequestConfig;
    if (error.response?.status === 401 && !originalRequest._retry) {
      const { clearAuth, setAuth } = useAuthStore.getState();
      originalRequest._retry = true;
      try {
        const refreshed = await refresh();
        setAuth(refreshed.user, refreshed.token, refreshed.expiresIn);
        originalRequest.headers = AxiosHeaders.from(originalRequest.headers ?? {});
        originalRequest.headers.set("Authorization", `Bearer ${refreshed.token}`);
        return authorizedRequest(originalRequest);
      } catch {
        clearAuth();
      }
    }
    throw error;
  },
);

export default authorizedRequest;
