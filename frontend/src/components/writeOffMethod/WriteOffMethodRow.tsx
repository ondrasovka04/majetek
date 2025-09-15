import { useState, useRef } from "react";
import { TableRow, TableCell, TextField, IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { WriteOffMethod } from "../../types/writeOffMethod";
import {
  updateWriteOffMethod,
  deleteWriteOffMethod,
} from "../../services/writeOffMethodService";
import { translateError } from "../../utils/errorTranslations";

interface Props {
  method: WriteOffMethod;
  onDelete: (id: string) => void;
  onUpdate: (id: string, name: string) => void;
  onError: (msg: string) => void;
}

const WriteOffMethodRow = ({ method, onDelete, onUpdate, onError }: Props) => {
  const [name, setName] = useState(method.name);
  const lastSentName = useRef(method.name);

  const handleBlur = async () => {
    if (name === lastSentName.current) return;

    if (name && name.length > 255) {
      onError("Název nesmí být delší než 255 znaků.");
      return;
    }

    const trimmed = name?.trim() === "" ? null : name?.trim() ?? null;

    try {
      await updateWriteOffMethod(method.id, trimmed);
      lastSentName.current = name;
      onUpdate(method.id, trimmed ?? "");
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  const handleDelete = async () => {
    try {
      await deleteWriteOffMethod(method.id);
      onDelete(method.id);
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
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
          value={name}
          onChange={(e) => setName(e.target.value)}
          onBlur={handleBlur}
          fullWidth
          size="small"
          autoComplete="off"
        />
      </TableCell>
      <TableCell>
        <IconButton
          color="error"
          onClick={handleDelete}
          size="small"
          aria-label="Smazat"
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      </TableCell>
    </TableRow>
  );
};

export default WriteOffMethodRow;
