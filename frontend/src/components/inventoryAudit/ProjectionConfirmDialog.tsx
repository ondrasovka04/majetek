/** Confirmation dialog shown before projecting inventory audit results. Displays a summary of actions (keep, relocate, write-off counts). */
import React from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from "@mui/material";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import DeleteForeverIcon from "@mui/icons-material/DeleteForever";

interface ProjectionConfirmDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  totalCount: number;
  writeOffCount: number;
  warningCount: number;
  wrongCount: number;
}

const ProjectionConfirmDialog: React.FC<ProjectionConfirmDialogProps> = ({
  open,
  onClose,
  onConfirm,
  totalCount,
  writeOffCount,
  warningCount,
  wrongCount,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Potvrdit promítnutí inventury</DialogTitle>
      <DialogContent dividers>
        <Typography gutterBottom>Chystáte se promítnout provedené akce do systému. Tento krok je nevratný.</Typography>
        <List dense>
          <ListItem>
            <ListItemIcon>
              <CheckCircleIcon color="success" />
            </ListItemIcon>
            <ListItemText primary={`Celkem položek: ${totalCount}`} />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <WarningAmberIcon color="warning" />
            </ListItemIcon>
            <ListItemText primary={`Nalezeno jinde: ${warningCount}`} />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <DeleteForeverIcon color="error" />
            </ListItemIcon>
            <ListItemText primary={`K vyřazení: ${writeOffCount}`} />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <WarningAmberIcon color="error" />
            </ListItemIcon>
            <ListItemText primary={`Nenaskenováno: ${wrongCount}`} />
          </ListItem>
        </List>
        <Typography
          variant="body2"
          sx={{
            color: "text.secondary",
          }}>
          Zkontrolujte prosím, že všechny položky mají platné akce a vyřazení obsahuje datum i způsob.
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Zrušit</Button>
        <Button variant="contained" color="primary" onClick={onConfirm}>
          Promítnout
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ProjectionConfirmDialog;
