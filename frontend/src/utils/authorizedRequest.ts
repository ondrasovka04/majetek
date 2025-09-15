import axios from "axios";
import { useAuthStore } from "../store/authStore";

const API_URL = process.env.REACT_APP_API_URL;

const authorizedRequest = axios.create({
  baseURL: API_URL,
});

authorizedRequest.interceptors.request.use((config) => {
  const { token, expiresIn, logout } = useAuthStore.getState();

  if (token) {
    if (expiresIn) {
      const expiryDate = new Date(expiresIn);
      const now = new Date();

      if (expiryDate <= now) {
        logout();
        throw new axios.Cancel("Token expired – user logged out.");
      }
    }

    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default authorizedRequest;
