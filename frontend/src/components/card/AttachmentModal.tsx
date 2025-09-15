import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Typography,
  Box,
  CircularProgress,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import CloudDownloadIcon from "@mui/icons-material/CloudDownload";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import { useEffect, useRef, useState, useCallback } from "react";
import { AttachmentDto } from "../../types/attachment";
import {
  getAttachments,
  deleteAttachment,
  uploadAttachment,
  downloadAttachment,
} from "../../services/attachmentService";
import { saveAs } from "file-saver";
import { translateError } from "../../utils/errorTranslations";

interface Props {
  open: boolean;
  onClose: () => void;
  cardId: string;
  onAttachmentChange?: () => void;
  onError: (msg: string) => void;
}

const AttachmentModal = ({
  open,
  onClose,
  cardId,
  onAttachmentChange,
  onError,
}: Props) => {
  const [attachments, setAttachments] = useState<AttachmentDto[]>([]);
  const [uploading, setUploading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const fetch = useCallback(async () => {
    const data = await getAttachments(cardId);
    setAttachments(data);
  }, [cardId]);

  useEffect(() => {
    if (open) {
      fetch();
    }
  }, [open, fetch]);

  const handleDelete = async (id: number) => {
    await deleteAttachment(cardId, id);
    await fetch();
    onAttachmentChange?.();
  };

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    try {
      await uploadAttachment(cardId, file);
      await fetch();
    } catch (err: any) {
      if (err.message === "Request failed with status code 413") {
        onError("Příloha je příliš velká");
        return;
      }
      const backendMessage = err.response?.data?.message;
      onError(translateError(backendMessage));
    } finally {
      setUploading(false);
    }

    onAttachmentChange?.();
  };

  const handleDownload = async (id: number, name: string) => {
    try {
      const blob = await downloadAttachment(cardId, id);
      saveAs(blob, name);
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      onError(translateError(backendMessage));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Přílohy</DialogTitle>
      <DialogContent>
        <input ref={inputRef} type="file" hidden onChange={handleUpload} />
        <Box display="flex" justifyContent="flex-end" mb={2}>
          <Button
            startIcon={<UploadFileIcon />}
            onClick={() => inputRef.current?.click()}
            disabled={uploading}
          >
            Nahrát soubor
          </Button>
        </Box>

        {uploading ? (
          <Box display="flex" alignItems="center" gap={1}>
            <CircularProgress size={20} />
            <Typography variant="body2" color="text.secondary">
              Nahrávám soubor…
            </Typography>
          </Box>
        ) : attachments.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            Žádné přílohy
          </Typography>
        ) : (
          <List dense>
            {attachments.map((a) => (
              <ListItem
                key={a.id}
                secondaryAction={
                  <>
                    <IconButton
                      edge="end"
                      onClick={() => handleDownload(a.id, a.name)}
                    >
                      <CloudDownloadIcon />
                    </IconButton>
                    <IconButton edge="end" onClick={() => handleDelete(a.id)}>
                      <DeleteIcon />
                    </IconButton>
                  </>
                }
              >
                <ListItemText primary={a.name} />
              </ListItem>
            ))}
          </List>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={uploading}>
          Zavřít
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AttachmentModal;
