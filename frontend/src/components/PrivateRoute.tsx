/** Route guard that redirects unauthenticated users to the login page. */
import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

interface PrivateRouteProps {
  children: ReactNode;
}

const PrivateRoute = ({ children }: PrivateRouteProps) => {
  const { token, hydrated } = useAuthStore();
  
  if (!hydrated) {
    return <div style={{padding: '1rem', fontSize: 14, color: '#666'}}>Kontroluji relaci…</div>;
  }

  if (!token) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default PrivateRoute;
