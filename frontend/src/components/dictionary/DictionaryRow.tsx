/** Editable table row for a dictionary entity — inline name editing with validation and delete action. */
import { useState, useRef, memo } from "react";
import { TableRow, TableCell, TextField, IconButton, Box } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { DictionaryEntity } from "@/types/dictionary";
import { dictionaryNameSchema } from "@/schemas/dictionarySchema";

interface Props<T extends DictionaryEntity> {
  readonly item: T;
  readonly onUpdate: (vars: { id: string; name: string | null }) => void;
  readonly onDelete: (id: string) => void;
  readonly onError: (msg: string) => void;
  /** Extra action buttons rendered after the delete button */
  readonly extraActions?: (item: T) => React.ReactNode;
}

function DictionaryRowInner<T extends DictionaryEntity>({ item, onUpdate, onDelete, onError, extraActions }: Props<T>) {
  const [name, setName] = useState(item.name ?? "");
  const lastSentName = useRef(item.name ?? "");

  const handleBlur = () => {
    const trimmed = name.trim() === "" ? null : name.trim();
    const normalised = trimmed ?? "";
    if (normalised === lastSentName.current) return;

    const result = dictionaryNameSchema.safeParse(name);
    if (!result.success) {
      onError(result.error.issues[0]?.message ?? "Neplatný název.");
      return;
    }

    onUpdate({ id: item.id, name: trimmed });
    lastSentName.current = normalised;
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
        }}>
        {item.id}
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
        <Box
          sx={{
            display: "flex",
            gap: 1,
          }}>
          {extraActions?.(item)}
          <IconButton color="error" onClick={() => onDelete(item.id)} size="small" aria-label="Smazat">
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      </TableCell>
    </TableRow>
  );
}

const DictionaryRow = memo(DictionaryRowInner) as typeof DictionaryRowInner;
export default DictionaryRow;
