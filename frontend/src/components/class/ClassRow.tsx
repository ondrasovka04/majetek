import { useState, useRef } from "react";
import { TableRow, TableCell, TextField, IconButton } from "@mui/material";
import { Class } from "../../types/class";
import { updateClass, deleteClass } from "../../services/classService";
import DeleteIcon from "@mui/icons-material/Delete";
import { translateError } from "../../utils/errorTranslations";

interface Props {
  cls: Class;
  onDelete: (id: string) => void;
  onUpdate: (id: string, name: string) => void;
  onError: (msg: string) => void;
}

const ClassRow = ({ cls, onDelete, onUpdate, onError }: Props) => {
  const [name, setName] = useState(cls.name);
  const lastSentName = useRef(cls.name);

  const handleBlur = async () => {
    if (name === lastSentName.current) return;
    if (name && name.length > 255) {
      onError("Název nesmí být delší než 255 znaků.");
      return;
    }

    const trimmed = name?.trim() === "" ? null : name?.trim() ?? null;

    try {
      await updateClass(cls.id, trimmed);
      lastSentName.current = name;
      onUpdate(cls.id, trimmed ?? "");
    } catch (error: any) {
      onError(translateError(error.response?.data?.message));
    }
  };

  const handleDelete = async () => {
    try {
      await deleteClass(cls.id);
      onDelete(cls.id);
    } catch (error: any) {
      onError(translateError(error.response?.data?.message));
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
        {cls.id}
      </TableCell>
      <TableCell>
        <TextField
          value={name}
          onChange={(e) => setName(e.target.value)}
          onBlur={handleBlur}
          fullWidth
          size="small"
        />
      </TableCell>
      <TableCell>
        <IconButton color="error" onClick={handleDelete} size="small">
          <DeleteIcon fontSize="small" />
        </IconButton>
      </TableCell>
    </TableRow>
  );
};

export default ClassRow;
