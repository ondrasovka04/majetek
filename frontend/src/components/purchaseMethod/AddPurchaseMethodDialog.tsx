import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Typography,
  ThemeProvider,
  CssBaseline
} from "@mui/material";
import { useState } from "react";
import { PurchaseMethod } from "../../types/purchaseMethod";
import { createPurchaseMethod } from "../../services/purchaseMethodService";
import { translateError } from "../../utils/errorTranslations";
import { defaultTheme } from "../../theme";

interface Props {
  open: boolean;
  onClose: () => void;
  onCreated: (method: PurchaseMethod) => void;
}

const AddPurchaseMethodDialog = ({ open, onClose, onCreated }: Props) => {
  const [id, setId] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!id.trim()) return setError("ID je povinné.");
    if (id.length > 20) return setError("ID nesmí být delší než 20 znaků.");
    if (name.length > 255) return setError("Název nesmí být delší než 255 znaků.");

    try {
      const method = await createPurchaseMethod({ id, name: name.trim() === "" ? null : name });
      onCreated(method);
      setId("");
      setName("");
      setError("");
      onClose();
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      setError(translateError(backendMessage));
    }
  };

  return (
    <ThemeProvider theme={defaultTheme}>
          <CssBaseline />
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Nový způsob pořízení</DialogTitle>
      <DialogContent>
        <TextField
          label="ID"
          fullWidth
          margin="dense"
          value={id}
          onChange={(e) => setId(e.target.value)}
        />
        <TextField
          label="Název"
          fullWidth
          margin="dense"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        {error && <Typography color="error">{error}</Typography>}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Zrušit</Button>
        <Button variant="contained" onClick={handleSubmit}>
          Vytvořit
        </Button>
      </DialogActions>
    </Dialog>
    </ThemeProvider>
  );
};

export default AddPurchaseMethodDialog;
