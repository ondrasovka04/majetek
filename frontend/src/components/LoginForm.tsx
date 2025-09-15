import { useState } from "react";
import {
  Button,
  TextField,
  Card,
  Typography,
  CircularProgress,
} from "@mui/material";
import { login } from "../services/authService";
import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom";
import { translateError } from "../utils/errorTranslations";

const LoginForm = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setIsLoading(true);
    try {
      const { user, token, expiresIn } = await login(username, password);
      setAuth(user, token, expiresIn);
      navigate("/card");
    } catch (error: any) {
      const backandMessage = error.response?.data?.message;
      setError(translateError(backandMessage));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="max-w-sm mx-auto p-6 shadow-lg">
      <Typography variant="h5" className="mb-4">
        Přihlášení
      </Typography>
      <form onSubmit={handleSubmit}>
        <TextField
          label="Uživatelské jméno"
          fullWidth
          margin="normal"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <TextField
          label="Heslo"
          type="password"
          fullWidth
          margin="normal"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <Typography color="error">{error}</Typography>}
        <Button type="submit" variant="contained" fullWidth className="mt-4">
          {isLoading ? <CircularProgress size={24} color="inherit" /> : "Přihlásit se"}
        </Button>
      </form>
    </Card>
  );
};

export default LoginForm;
