import axios from "axios";
import { useAuthStore } from "../store/authStore";
import { refresh } from "../services/authService";

const API_URL = process.env.REACT_APP_API_URL;

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
    } catch (e) {
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
    const originalRequest = error.config;
    if (error.response?.status === 401 && !(originalRequest as any)._retry) {
      const { clearAuth, setAuth } = useAuthStore.getState();
      (originalRequest as any)._retry = true;
      try {
        const refreshed = await refresh();
        setAuth(refreshed.user, refreshed.token, refreshed.expiresIn);
        if (!originalRequest.headers) originalRequest.headers = {};
        (originalRequest.headers as any).Authorization = `Bearer ${refreshed.token}`;
        return authorizedRequest(originalRequest);
      } catch (e) {
        clearAuth();
      }
    }
    return Promise.reject(error);
  }
);

export default authorizedRequest;
