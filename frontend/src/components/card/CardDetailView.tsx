import {
  TextField,
  Grid,
  MenuItem,
  Typography,
  CircularProgress,
  Collapse,
  Alert,
  Tooltip,
  IconButton,
  Box,
} from "@mui/material";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import AttachmentModal from "./AttachmentModal";
import { getAttachments } from "../../services/attachmentService";
import { AttachmentDto } from "../../types/attachment";
import SaveIcon from "@mui/icons-material/Save";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { DatePicker } from "@mui/x-date-pickers";
import { useEffect, useState } from "react";
import {
  getCardById,
  updateCard,
  deleteCard,
  exportCardPdf,
} from "../../services/cardService";
import { CardDetail } from "../../types/card/cardDetail";
import { translateError } from "../../utils/errorTranslations";
import { Class } from "../../types/class";
import { AssetGroup } from "../../types/assetGroup";
import { Location } from "../../types/location";
import { PurchaseMethod } from "../../types/purchaseMethod";
import { WriteOffMethod } from "../../types/writeOffMethod";
import { User } from "../../types/auth";
import DeleteIcon from "@mui/icons-material/Delete";
import DownloadIcon from "@mui/icons-material/Download";
import ConfirmDialog from "./ConfirmDialog";
import { PriceInput } from "./PriceInput";
import { saveAs } from "file-saver";

interface Props {
  cardId: string;
  onChange?: (newCard: CardDetail) => void;
  onDeleted?: () => void;
  onDuplicate?: (duplicatedCard: Partial<CardDetail>) => void;
  locations: Location[];
  groups: AssetGroup[];
  classes: Class[];
  purchaseMethods: PurchaseMethod[];
  writeOffMethods: WriteOffMethod[];
  users: User[];
}

const CardDetailView = ({
  cardId,
  onChange,
  onDeleted,
  onDuplicate,
  locations,
  groups,
  classes,
  purchaseMethods,
  writeOffMethods,
  users,
}: Props) => {
  const [card, setCard] = useState<CardDetail | null>(null);
  const [edited, setEdited] = useState<CardDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const [attachments, setAttachments] = useState<AttachmentDto[]>([]);
  const [attachmentModalOpen, setAttachmentModalOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cardDetail, attch] = await Promise.all([
          getCardById(cardId),
          getAttachments(cardId),
        ]);

        setAttachments(attch);
        setCard(cardDetail);
        setEdited(cardDetail);
      } catch (e: any) {
        handleError(translateError(e.message));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [cardId]);

  const handleFieldChange = (field: keyof CardDetail, value: any) => {
    if (!edited) return;
    setEdited({ ...edited, [field]: value === "" ? null : value });
  };

  const handleSave = async () => {
    if (!edited) return;

    if (!edited.name || edited.name === "") {
      handleError("Název nemůže být prazdný");
      return;
    }

    if (edited.name.length > 100) {
      handleError("Název může mít maximálně 100 znaků");
      return;
    }

    if (edited.purchaseInvoice && edited.purchaseInvoice.length > 30) {
      handleError("Faktura může mít maximálně 30 znaků");
      return;
    }

    if (edited.price != null && !isValidDecimal(edited.price, 36, 2)) {
      handleError(
        "Cena musí být číslo s maximálně 2 desetinnými místy a 36 číslicemi celkem."
      );
      return;
    }

    if (edited.quantity != null && !isValidDecimal(edited.quantity, 36, 2)) {
      handleError(
        "Počet kusů musí být číslo s maximálně 2 desetinnými místy a 36 číslicemi celkem."
      );
      return;
    }

    if (edited.serialNumber != null && edited.serialNumber.length > 30) {
      handleError("Výrobní číslo může mít maximálně 30 znaků");
      return;
    }

    if (isNaN(new Date(edited.purchaseDate).getTime())) {
      handleError("Neplatné datum pořízení.");
      return;
    }

    if (edited.writeOffDate && isNaN(new Date(edited.writeOffDate).getTime())) {
      handleError("Neplatné datum vyřazení.");
      return;
    }

    if (
      (edited.writeOffDate && !edited.writeOffMethodId) ||
      (!edited.writeOffDate && edited.writeOffMethodId)
    ) {
      handleError(
        "Při vyřazení majetku se musí uvést datum i způsob vyřazení."
      );
      return;
    }

    setSaving(true);
    try {
      await updateCard(edited.id, edited);
      setCard(edited);
      if (
        (edited.name !== card?.name ||
          edited.writeOffDate !== card?.writeOffDate) &&
        onChange
      ) {
        onChange(edited);
      }

      handleSuccess("Změny byly úspěšně uloženy.");
    } catch (e: any) {
      handleError(translateError(e.message));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!card) return;
    try {
      await deleteCard(card.id);
      onDeleted?.();
    } catch (e: any) {
      handleError(translateError(e.message));
    } finally {
      setConfirmOpen(false);
    }
  };

  const handleReportDownload = async () => {
    if (!card) return;
    try {
      const blob = (await exportCardPdf(Number(card.id))).blob;
      saveAs(blob, `karta-${card.id}.pdf`);
    } catch (e: any) {
      handleError(translateError(e.message));
    } finally {
      handleSuccess("Sestava byla úspěšně stažena.");
    }
  };

  const isValidDecimal = (
    value: any,
    maxInteger: number,
    maxFraction: number
  ): boolean => {
    if (typeof value !== "number" || isNaN(value)) return false;

    const [intPart, fracPart = ""] = value.toString().split(".");

    return intPart.length <= maxInteger && fracPart.length <= maxFraction;
  };

  const handleError = (msg: string) => {
    setSuccessMessage("");
    setError(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  const handleSuccess = (msg: string) => {
    setError("");
    setSuccessMessage(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  if (loading || !card) {
    return <CircularProgress />;
  }

  return (
    <div>
      <div
        style={{
          position: "fixed",
          top: 20,
          left: "50%",
          transform: "translateX(-50%)",
          zIndex: 1400,
          width: "auto",
          maxWidth: "90vw",
        }}
      >
        <Collapse in={snackbarOpen}>
          <Alert
            severity={successMessage ? "success" : "error"}
            variant="filled"
            onClose={() => setSnackbarOpen(false)}
            sx={{ mb: 2 }}
          >
            {successMessage || error}
          </Alert>
        </Collapse>
      </div>

      <div className="flex items-center justify-between mb-2">
        <Typography variant="h6">Karta #{edited?.id}</Typography>
        <div className="flex items-center justify-between mb-2">
          <Tooltip title="Uložit změny">
            <span>
              <IconButton
                onClick={handleSave}
                color="primary"
                disabled={saving}
              >
                {saving ? <CircularProgress size={24} /> : <SaveIcon />}
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Duplikovat">
            <span>
              <IconButton
                onClick={() =>
                  edited &&
                  onDuplicate?.({
                    ...edited,
                    id: undefined,
                    createdDate: undefined,
                    createdById: undefined,
                  })
                }
                color="primary"
              >
                <ContentCopyIcon />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Spravovat přílohy">
            <span>
              <IconButton
                onClick={() => setAttachmentModalOpen(true)}
                color={attachments.length > 0 ? "primary" : "default"}
              >
                <AttachFileIcon />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Stáhnout sestavu">
            <span>
              <IconButton
                onClick={() => handleReportDownload()}
                color="primary"
              >
                <DownloadIcon />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Smazat kartu">
            <span>
              <IconButton onClick={() => setConfirmOpen(true)} color="error">
                <DeleteIcon />
              </IconButton>
            </span>
          </Tooltip>
        </div>
      </div>

      <div
        style={{
          maxHeight: "calc(100vh - 180px)",
          overflowY: "auto",
          paddingRight: 8,
          paddingTop: 8,
        }}
      >
        <Grid container spacing={2}>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Název"
              fullWidth
              value={edited?.name ?? ""}
              onChange={(e) => handleFieldChange("name", e.target.value)}
              error={!edited?.name}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <PriceInput
              label="Cena"
              value={edited?.price ?? null}
              onChange={(val) => handleFieldChange("price", val)}
            />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Počet kusů"
              type="number"
              fullWidth
              value={edited?.quantity ?? ""}
              onChange={(e) =>
                handleFieldChange("quantity", parseFloat(e.target.value))
              }
            />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Sériové číslo"
              fullWidth
              value={edited?.serialNumber ?? ""}
              onChange={(e) =>
                handleFieldChange("serialNumber", e.target.value)
              }
            />
          </Grid>

          <Grid size={{ xs: 12 }} sx={{ my: 2 }} />

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Umístění"
              select
              fullWidth
              value={edited?.locationId ?? ""}
              onChange={(e) => handleFieldChange("locationId", e.target.value)}
              error={!edited?.locationId}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}
            >
              {locations.map((loc) => (
                <MenuItem key={loc.id} value={loc.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{loc.id}</span>
                    <span>{loc.name}</span>
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Třída"
              select
              fullWidth
              value={edited?.classId ?? ""}
              onChange={(e) => handleFieldChange("classId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}
            >
              <MenuItem value="">(nevybráno)</MenuItem>
              {classes.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{c.id}</span>
                    <span>{c.name}</span>
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Skupina"
              select
              fullWidth
              value={edited?.groupId ?? ""}
              onChange={(e) => handleFieldChange("groupId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}
            >
              {groups.map((g) => (
                <MenuItem key={g.id} value={g.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{g.id}</span>
                    <span>{g.name}</span>
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Způsob pořízení"
              select
              fullWidth
              value={edited?.purchaseMethodId ?? ""}
              onChange={(e) =>
                handleFieldChange("purchaseMethodId", e.target.value)
              }
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}
            >
              <MenuItem value="">(nevybráno)</MenuItem>
              {purchaseMethods.map((m) => (
                <MenuItem key={m.id} value={m.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{m.id}</span>
                    <span>{m.name}</span>
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Faktura"
              fullWidth
              value={edited?.purchaseInvoice ?? ""}
              onChange={(e) =>
                handleFieldChange("purchaseInvoice", e.target.value)
              }
            />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Datum pořízení"
              value={edited?.purchaseDate ?? null}
              onChange={(date) => handleFieldChange("purchaseDate", date)}
              slotProps={{
                textField: { fullWidth: true, error: !edited?.purchaseDate },
              }}
            />
          </Grid>

          <Grid size={{ xs: 12 }} sx={{ my: 2 }} />

          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Datum vyřazení"
              value={edited?.writeOffDate ?? null}
              onChange={(date) => handleFieldChange("writeOffDate", date)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Způsob vyřazení"
              select
              fullWidth
              value={edited?.writeOffMethodId ?? ""}
              onChange={(e) =>
                handleFieldChange("writeOffMethodId", e.target.value)
              }
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}
            >
              <MenuItem value="">(nevybráno)</MenuItem>
              {writeOffMethods.map((m) => (
                <MenuItem key={m.id} value={m.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{m.id}</span>
                    <span>{m.name}</span>
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Zodpovědná osoba"
              select
              fullWidth
              value={edited?.responsiblePersonId ?? ""}
              onChange={(e) =>
                handleFieldChange("responsiblePersonId", e.target.value)
              }
            >
              <MenuItem value="">(nevybráno)</MenuItem>
              {users.map((u) => (
                <MenuItem key={u.id} value={u.id}>
                  {u.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          <Grid size={{ xs: 12 }}>
            <TextField
              label="Poznámka"
              fullWidth
              multiline
              minRows={2}
              value={edited?.note ?? ""}
              onChange={(e) => handleFieldChange("note", e.target.value)}
            />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Vytvořeno"
              fullWidth
              value={edited?.createdDate.toLocaleDateString("cs-CZ")}
              disabled
            />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Vytvořil"
              fullWidth
              value={edited?.createdById}
              disabled
            />
          </Grid>
        </Grid>
      </div>
      <AttachmentModal
        open={attachmentModalOpen}
        onClose={() => setAttachmentModalOpen(false)}
        cardId={cardId}
        onAttachmentChange={async () => {
          const attachments = await getAttachments(cardId);
          setAttachments(attachments);
        }}
        onError={handleError}
      />
      <ConfirmDialog
        open={confirmOpen}
        title="Smazat kartu?"
        description="Opravdu chcete smazat tuto kartu? Tuto akci nelze vrátit zpět."
        onCancel={() => setConfirmOpen(false)}
        onConfirm={handleDelete}
      />
    </div>
  );
};

export default CardDetailView;
