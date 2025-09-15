import { useState, useRef } from "react";
import { TableRow, TableCell, TextField, IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { PurchaseMethod } from "../../types/purchaseMethod";
import {
  updatePurchaseMethod,
  deletePurchaseMethod,
} from "../../services/purchaseMethodService";
import { translateError } from "../../utils/errorTranslations";

interface Props {
  method: PurchaseMethod;
  onDelete: (id: string) => void;
  onUpdate: (id: string, name: string) => void;
  onError: (msg: string) => void;
}

const PurchaseMethodRow = ({ method, onDelete, onUpdate, onError }: Props) => {
  const [name, setName] = useState(method.name);
  const lastSent = useRef(method.name);

  const handleBlur = async () => {
    if (name === lastSent.current) return;
    if (name && name.length > 255) {
      onError("Název nesmí být delší než 255 znaků.");
      return;
    }

    const trimmed = name?.trim() === "" ? null : name?.trim() ?? null;

    try {
      await updatePurchaseMethod(method.id, trimmed);
      lastSent.current = name;
      onUpdate(method.id, trimmed ?? "");
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  const handleDelete = async () => {
    try {
      await deletePurchaseMethod(method.id);
      onDelete(method.id);
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  return (
    <TableRow hover>
      <TableCell
        sx={{
          width: 200,
          maxWidth: 200,
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
        }}
      >
        {method.id}
      </TableCell>
      <TableCell>
        <TextField
          fullWidth
          size="small"
          value={name}
          onChange={(e) => setName(e.target.value)}
          onBlur={handleBlur}
        />
      </TableCell>
      <TableCell>
        <IconButton onClick={handleDelete} color="error" size="small">
          <DeleteIcon fontSize="small" />
        </IconButton>
      </TableCell>
    </TableRow>
  );
};

export default PurchaseMethodRow;
