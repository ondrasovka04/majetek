/** Generic confirmation dialog with title, description, and confirm/cancel actions. */
import { Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from "@mui/material";

interface ConfirmDialogProps {
  open: boolean;
  title?: string;
  description: string;
  onCancel: () => void;
  onConfirm: () => void;
}

const ConfirmDialog = ({ open, title = "Potvrzení akce", description, onCancel, onConfirm }: ConfirmDialogProps) => {
  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{description}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Zrušit</Button>
        <Button onClick={onConfirm} variant="contained" color="primary">
          Potvrdit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
