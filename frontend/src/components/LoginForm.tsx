/** Login form with username/password fields. Calls the auth service directly (no TanStack Query — one-time action). */
import { Button, TextField, Card, Typography, CircularProgress } from "@mui/material";
import { useForm } from "react-hook-form";
import { useState } from "react";
import { login } from "../services/authService";
import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom";
import { translateError } from "../utils/errorTranslations";
import { extractErrorMessage } from "../utils/extractErrorMessage";

interface LoginFormValues {
  username: string;
  password: string;
}

const LoginForm = () => {
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  const { register, handleSubmit } = useForm<LoginFormValues>({
    defaultValues: { username: "", password: "" },
  });

  const onSubmit = async (data: LoginFormValues) => {
    setIsLoading(true);
    setError("");
    try {
      const { user, token, expiresIn } = await login(data.username, data.password);
      setAuth(user, token, expiresIn);
      navigate("/card");
    } catch (err: unknown) {
      setError(translateError(extractErrorMessage(err)));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="max-w-sm mx-auto p-6 shadow-lg">
      <Typography variant="h5" className="mb-4">
        Přihlášení
      </Typography>
      <form onSubmit={handleSubmit(onSubmit)}>
        <TextField label="Uživatelské jméno" fullWidth margin="normal" {...register("username")} />
        <TextField label="Heslo" type="password" fullWidth margin="normal" {...register("password")} />
        {error && <Typography color="error">{error}</Typography>}
        <Button type="submit" variant="contained" fullWidth className="mt-4">
          {isLoading ? <CircularProgress size={24} color="inherit" /> : "Přihlásit se"}
        </Button>
      </form>
    </Card>
  );
};

export default LoginForm;
