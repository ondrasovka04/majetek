import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { RegisterUserDto } from "../types/auth";
import { registerUser } from "../services/authService";
import { translateError } from "../utils/errorTranslations";

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess: (message: string) => void;
}

const AdminUserDialog = ({ open, onClose, onSuccess }: Props) => {
  const [form, setForm] = useState<RegisterUserDto>({
    id: "",
    name: "",
    username: "",
    password: "",
  });

  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!form.id.trim()) {
      setError("ID nesmí být prázdné.");
      return;
    }

    if (!form.username.trim()) {
      setError("Uživatelské jméno nesmí být prázdné.");
      return;
    }

    if (!form.password.trim()) {
      setError("Heslo nesmí být prázdné.");
      return;
    }

    try {
      await registerUser(form);
      onSuccess(`Uživatel ${form.username} byl úspěšně vytvořen.`);
      setForm({ id: "", name: "", username: "", password: "" });
      setError("");
      onClose();
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      setError(translateError(backendMessage));
    }
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Vytvořit nového uživatele</DialogTitle>
      <DialogContent>
        <TextField
          label="ID"
          fullWidth
          sx={{ mb: 2, mt: 1 }}
          value={form.id}
          onChange={(e) => setForm({ ...form, id: e.target.value })}
        />
        <TextField
          label="Uživatelské jméno"
          fullWidth
          sx={{ mb: 2 }}
          value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })}
        />
        <TextField
          label="Jméno"
          fullWidth
          sx={{ mb: 2 }}
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <TextField
          label="Heslo"
          type="password"
          fullWidth
          sx={{ mb: 2 }}
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />
        {error && <Typography color="error">{error}</Typography>}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Zrušit</Button>
        <Button onClick={handleSubmit} variant="contained">
          Vytvořit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AdminUserDialog;
