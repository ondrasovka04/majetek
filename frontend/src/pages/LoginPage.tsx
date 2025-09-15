import LoginForm from "../components/LoginForm";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../store/authStore";

const LoginPage = () => {
  const token = useAuthStore((state) => state.token);
  const expiresIn = useAuthStore((state) => state.expiresIn);
  const navigate = useNavigate();

  useEffect(() => {
    if (token && expiresIn) {
      const expiryDate = new Date(expiresIn);
      if (expiryDate > new Date()) {
        navigate("/card");
      }
    }
  }, [token, expiresIn, navigate]);

  return (
    <div className="min-h-screen bg-gray-100 flex justify-center items-start pt-28">
      <LoginForm />
    </div>
  );
};

export default LoginPage;
