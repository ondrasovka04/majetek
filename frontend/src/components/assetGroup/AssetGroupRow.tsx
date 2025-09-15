import { useState, useRef } from "react";
import { TableRow, TableCell, TextField, IconButton } from "@mui/material";
import { AssetGroup } from "../../types/assetGroup";
import {
  updateAssetGroup,
  deleteAssetGroup,
} from "../../services/assetGroupService";
import { translateError } from "../../utils/errorTranslations";
import DeleteIcon from "@mui/icons-material/Delete";

interface Props {
  assetGroup: AssetGroup;
  onDelete: (id: string) => void;
  onUpdate: (id: string, newName: string) => void;
  onError: (msg: string) => void;
}

const AssetGroupRow = ({ assetGroup, onDelete, onUpdate, onError }: Props) => {
  const [name, setName] = useState(assetGroup.name);
  const lastSentName = useRef(assetGroup.name);

  const handleBlur = async () => {
    if (name === lastSentName.current) return;
    if (name.length > 255) {
      onError("Název nesmí být delší než 255 znaků.");
      return;
    }

    try {
      await updateAssetGroup(assetGroup.id, name);
      lastSentName.current = name;
      onUpdate(assetGroup.id, name);
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  const handleDelete = async () => {
    try {
      await deleteAssetGroup(assetGroup.id);
      onDelete(assetGroup.id);
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
        {assetGroup.id}
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

export default AssetGroupRow;
