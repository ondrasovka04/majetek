/**
 * Zustand auth store. Holds the current user, JWT token, and expiration.
 * `attemptRehydrate` tries to restore the session from the httpOnly refresh cookie on app start.
 */
import { create } from "zustand";
import { User } from "@/types/auth";
import { refresh } from "@/services/authService";

interface AuthState {
  user: User | undefined;
  token: string | undefined;
  expiresIn: string | undefined;
  hydrated: boolean;
  setAuth: (user: User, token: string, expiresIn: string) => void;
  clearAuth: () => void;
  attemptRehydrate: () => Promise<void>;
}

let rehydratePromise: Promise<void> | null = null;

export const useAuthStore = create<AuthState>((set, get) => ({
  user: undefined,
  token: undefined,
  expiresIn: undefined,
  hydrated: false,
  setAuth: (user, token, expiresIn) => {
    set({ user, token, expiresIn });
  },
  clearAuth: () => {
    set({ user: undefined, token: undefined, expiresIn: undefined });
  },
  attemptRehydrate: async () => {
    if (get().hydrated) return;
    rehydratePromise ??= (async () => {
      try {
        const data = await refresh();
        get().setAuth(data.user, data.token, data.expiresIn);
      } catch {
        // ignore - stay logged out
      } finally {
        set({ hydrated: true });
        rehydratePromise = null;
      }
    })();
    return rehydratePromise;
  },
}));
