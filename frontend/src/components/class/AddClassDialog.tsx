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
import { Class } from "../../types/class";
import { createClass } from "../../services/classService";
import { translateError } from "../../utils/errorTranslations";
import { defaultTheme } from "../../theme";

interface Props {
  open: boolean;
  onClose: () => void;
  onCreated: (cls: Class) => void;
}

const AddClassDialog = ({ open, onClose, onCreated }: Props) => {
  const [id, setId] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!id) return setError("ID je povinné.");
    if (id.length > 10) return setError("ID nesmí být delší než 10 znaků.");
    if (name.length > 255)
      return setError("Název nesmí být delší než 255 znaků.");

    try {
      const newClass = await createClass({ id, name: name.trim() === "" ? null : name });
      onCreated(newClass);
      setId("");
      setName("");
      setError("");
      onClose();
    } catch (error: any) {
      setError(translateError(error.response?.data?.message));
    }
  };

  return (
    <ThemeProvider theme={defaultTheme}>
          <CssBaseline />
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Nová třída</DialogTitle>
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

export default AddClassDialog;
