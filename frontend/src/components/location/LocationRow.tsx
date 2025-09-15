import { useState, useRef } from "react";
import { TableRow, TableCell, TextField, IconButton, Box } from "@mui/material";
import { Location } from "../../types/location";
import {
  updateLocation,
  deleteLocation,
  printLocation,
} from "../../services/locationService";
import { translateError } from "../../utils/errorTranslations";
import DeleteIcon from "@mui/icons-material/Delete";
import PrintIcon from "@mui/icons-material/Print";

interface Props {
  location: Location;
  onDelete: (id: string) => void;
  onUpdate: (id: string, newName: string) => void;
  onError: (msg: string) => void;
  onSuccess: (msg: string) => void;
}

const LocationRow = ({
  location,
  onDelete,
  onError,
  onUpdate,
  onSuccess,
}: Props) => {
  const [name, setName] = useState(location.name);
  const lastSentName = useRef(location.name);

  const handleBlur = async () => {
    if (name === lastSentName.current) return;

    if (name && name.length > 255) {
      onError("Název nesmí být delší než 255 znaků.");
      return;
    }

    const trimmed = name?.trim() === "" ? null : name?.trim() ?? null;

    try {
      await updateLocation(location.id, trimmed);
      lastSentName.current = name;
      onUpdate(location.id, trimmed ?? "");
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  const handleDelete = async () => {
    try {
      await deleteLocation(location.id);
      onDelete(location.id);
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  const handlePrint = async () => {
    try {
      const data = await printLocation(location.id);
      onSuccess(data);
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  return (
    <TableRow hover>
      <TableCell
        sx={{
          width: 100,
          maxWidth: 100,
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
        }}
      >
        {location.id}
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
      <TableCell colSpan={2}>
        <Box display="flex" justifyContent="flex" gap={1}>
          <IconButton onClick={handlePrint} size="small" aria-label="Tisknout">
            <PrintIcon fontSize="small" />
          </IconButton>
          <IconButton
            color="error"
            onClick={handleDelete}
            size="small"
            aria-label="Smazat"
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      </TableCell>
    </TableRow>
  );
};

export default LocationRow;
