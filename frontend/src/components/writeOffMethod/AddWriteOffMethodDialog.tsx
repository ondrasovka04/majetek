import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Typography,
  ThemeProvider,
  CssBaseline,
} from "@mui/material";
import { useState } from "react";
import { WriteOffMethod } from "../../types/writeOffMethod";
import { createWriteOffMethod } from "../../services/writeOffMethodService";
import { translateError } from "../../utils/errorTranslations";
import { defaultTheme } from "../../theme";

interface Props {
  open: boolean;
  onClose: () => void;
  onCreated: (item: WriteOffMethod) => void;
}

const AddWriteOffMethodDialog = ({ open, onClose, onCreated }: Props) => {
  const [id, setId] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!id) {
      setError("ID je povinné.");
      return;
    }

    if (id.length > 20) {
      setError("ID nesmí být delší než 20 znaků.");
      return;
    }

    if (name.length > 255) {
      setError("Název nesmí být delší než 255 znaků.");
      return;
    }

    try {
      const newItem = await createWriteOffMethod({
        id,
        name: name.trim() === "" ? null : name,
      });
      onCreated(newItem);
      setId("");
      setName("");
      setError("");
      onClose();
    } catch (error: any) {
      const backandMessage = error.response?.data?.message;
      setError(translateError(backandMessage));
    }
  };

  return (
    <ThemeProvider theme={defaultTheme}>
      <CssBaseline />
      <Dialog open={open} onClose={onClose}>
        <DialogTitle>Nový způsob vyřazení</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="ID"
            fullWidth
            value={id}
            onChange={(e) => setId(e.target.value)}
          />
          <TextField
            margin="dense"
            label="Název"
            fullWidth
            value={name}
            onChange={(e) => setName(e.target.value)}
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
    </ThemeProvider>
  );
};

export default AddWriteOffMethodDialog;
