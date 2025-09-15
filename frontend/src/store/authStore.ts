import { create } from 'zustand';
import { User } from '../types/auth';

interface AuthState {
  user: User | null;
  token: string | null;
  expiresIn: string | null;
  setAuth: (user: User, token: string, expiresIn: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  expiresIn: null,
  setAuth: (user, token, expiresIn) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    localStorage.setItem('expiresIn', expiresIn);
    set({ user, token, expiresIn });
  },
  logout: () => {
    localStorage.clear();
    set({ user: null, token: null, expiresIn: null });
  },
}));
