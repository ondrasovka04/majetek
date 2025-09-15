import { ReactNode, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

interface PrivateRouteProps {
  children: ReactNode;
}

const PrivateRoute = ({ children }: PrivateRouteProps) => {
  const { token, expiresIn, logout } = useAuthStore();

  useEffect(() => {
    if (token && expiresIn) {
      const expiryDate = new Date(expiresIn);
      if (expiryDate <= new Date()) {
        logout();
      }
    }
  }, [token, expiresIn, logout]);

  if (!token) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default PrivateRoute;
