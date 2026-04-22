/** Login page. Redirects already-authenticated users to /card. */
import LoginForm from "@/components/LoginForm";
import { Navigate } from "react-router";
import { useAuthStore } from "@/store/authStore";

const LoginPage = () => {
  const token = useAuthStore((state) => state.token);
  const expiresIn = useAuthStore((state) => state.expiresIn);

  // Redirect already-authenticated users directly (no useEffect needed)
  if (token && expiresIn && new Date(expiresIn) > new Date()) {
    return <Navigate to="/card" replace />;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex justify-center items-start pt-28">
      <LoginForm />
    </div>
  );
};

export default LoginPage;
