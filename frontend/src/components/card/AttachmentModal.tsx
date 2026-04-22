/** Modal for managing card attachments — upload, download, and delete files. */
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
import { useRef } from "react";
import { downloadAttachment } from "@/services/attachmentService";
import { saveAs } from "file-saver";
import { translateError } from "@/utils/errorTranslations";
import { extractErrorMessage } from "@/utils/extractErrorMessage";
import { AxiosError } from "axios";
import { useAttachments, useUploadAttachment, useDeleteAttachment } from "@/hooks/queries/useAttachmentQueries";

interface Props {
  open: boolean;
  onClose: () => void;
  cardId: string;
  onError: (msg: string) => void;
}

const AttachmentModal = ({ open, onClose, cardId, onError }: Props) => {
  const { data: attachments = [] } = useAttachments(open ? cardId : undefined);
  const uploadMutation = useUploadAttachment(cardId);
  const deleteMutation = useDeleteAttachment(cardId);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id, {
      onError: (err: unknown) => {
        onError(translateError(extractErrorMessage(err)));
      },
    });
  };

  const handleUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    uploadMutation.mutate(file, {
      onError: (err: unknown) => {
        if (err instanceof AxiosError && err.response?.status === 413) {
          onError("Příloha je příliš velká");
          return;
        }
        onError(translateError(extractErrorMessage(err)));
      },
      onSettled: () => {
        // Reset file input so the same file can be uploaded again
        if (inputRef.current) inputRef.current.value = "";
      },
    });
  };

  const handleDownload = async (id: number, name: string) => {
    try {
      const blob = await downloadAttachment(cardId, id);
      saveAs(blob, name);
    } catch (err: unknown) {
      onError(translateError(extractErrorMessage(err)));
    }
  };

  const renderContent = () => {
    if (uploadMutation.isPending) {
      return (
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            gap: 1,
          }}>
          <CircularProgress size={20} />
          <Typography
            variant="body2"
            sx={{
              color: "text.secondary",
            }}>
            Nahrávám soubor…
          </Typography>
        </Box>
      );
    }
    if (attachments.length === 0) {
      return (
        <Typography
          variant="body2"
          sx={{
            color: "text.secondary",
          }}>
          Žádné přílohy
        </Typography>
      );
    }
    return (
      <List dense>
        {attachments.map((a) => (
          <ListItem
            key={a.id}
            secondaryAction={
              <>
                <IconButton edge="end" onClick={() => handleDownload(a.id, a.name)}>
                  <CloudDownloadIcon />
                </IconButton>
                <IconButton edge="end" onClick={() => handleDelete(a.id)}>
                  <DeleteIcon />
                </IconButton>
              </>
            }>
            <ListItemText primary={a.name} />
          </ListItem>
        ))}
      </List>
    );
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Přílohy</DialogTitle>
      <DialogContent>
        <input ref={inputRef} type="file" hidden onChange={handleUpload} />
        <Box
          sx={{
            display: "flex",
            justifyContent: "flex-end",
            mb: 2,
          }}>
          <Button
            startIcon={<UploadFileIcon />}
            onClick={() => inputRef.current?.click()}
            disabled={uploadMutation.isPending}>
            Nahrát soubor
          </Button>
        </Box>

        {renderContent()}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={uploadMutation.isPending}>
          Zavřít
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AttachmentModal;
