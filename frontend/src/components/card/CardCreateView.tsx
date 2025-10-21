import {
  TextField,
  Grid,
  MenuItem,
  Typography,
  IconButton,
  CircularProgress,
  Collapse,
  Alert,
  Tooltip,
  Box,
} from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import { DatePicker } from "@mui/x-date-pickers";
import { useState } from "react";
import { CardDetail } from "../../types/card/cardDetail";
import { createCard } from "../../services/cardService";
import { translateError } from "../../utils/errorTranslations";
import { Class } from "../../types/class";
import { AssetGroup } from "../../types/assetGroup";
import { Location } from "../../types/location";
import { PurchaseMethod } from "../../types/purchaseMethod";
import { WriteOffMethod } from "../../types/writeOffMethod";
import { User } from "../../types/auth";
import { PriceInput } from "./PriceInput";

interface Props {
  onCreated: () => void;
  onError: (msg: string) => void;
  onSuccess: (msg: string) => void;
  prefillCard?: Partial<CardDetail>;
  locations: Location[];
  groups: AssetGroup[];
  classes: Class[];
  purchaseMethods: PurchaseMethod[];
  writeOffMethods: WriteOffMethod[];
  users: User[];
}

const CardCreateView = ({
  onCreated,
  onError,
  onSuccess,
  prefillCard,
  locations,
  groups,
  classes,
  purchaseMethods,
  writeOffMethods,
  users,
}: Props) => {
  const [card, setCard] = useState<Partial<CardDetail>>({
    purchaseDate: new Date(),
    quantity: 1,
    classId: "DHM",
    ...prefillCard,
  });
  const [saving, setSaving] = useState(false);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (field: keyof CardDetail, value: any) => {
    setCard((prev) => ({ ...prev, [field]: value === "" ? null : value }));
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

  const handleSave = async () => {
    if (!card.name || card.name === "") {
      return raiseError("Název nemůže být prázdný.");
    }

    if (card.name.length > 100) {
      return raiseError("Název může mít maximálně 100 znaků.");
    }

    if (card.price != null && !isValidDecimal(card.price, 36, 2)) {
      return raiseError(
        "Cena musí být číslo s maximálně 2 desetinnými místy a 36 číslicemi celkem."
      );
    }

    if (card.quantity != null && !isValidDecimal(card.quantity, 36, 2)) {
      return raiseError(
        "Počet kusů musí být číslo s maximálně 2 desetinnými místy a 36 číslicemi celkem."
      );
    }

    if (card.serialNumber && card.serialNumber.length > 30) {
      return raiseError("Sériové číslo může mít maximálně 30 znaků.");
    }

    if (!card.locationId) {
      return raiseError("Musí být vybrané umístění.");
    }

    if (!card.groupId) {
      return raiseError("Musí být vybraná skupina.");
    }

    if (!card.purchaseDate) {
      return raiseError("Datum pořízení musí být zadáno.");
    }

    if (isNaN(new Date(card.purchaseDate).getTime())) {
      return raiseError("Neplatné datum pořízení.");
    }

    if (card.purchaseInvoice && card.purchaseInvoice.length > 30) {
      return raiseError("Faktura může mít maximálně 30 znaků.");
    }

    if (card.writeOffDate && isNaN(new Date(card.writeOffDate).getTime())) {
      return raiseError("Neplatné datum vyřazení.");
    }

    if (
      (card.writeOffDate && !card.writeOffMethodId) ||
      (!card.writeOffDate && card.writeOffMethodId)
    ) {
      return raiseError(
        "Při vyřazení majetku se musí uvést datum i způsob vyřazení."
      );
    }

    setSaving(true);
    try {
      await createCard(card);
      onSuccess("Karta úspěšně vytvořena.");
      onCreated();
    } catch (e: any) {
      raiseError(translateError(e.message));
    } finally {
      setSaving(false);
    }
  };

  const raiseError = (msg: string) => {
    setError(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
    onError(msg);
  };

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
            severity="error"
            variant="filled"
            onClose={() => setSnackbarOpen(false)}
          >
            {error}
          </Alert>
        </Collapse>
      </div>

      <div className="flex items-center justify-between mb-2">
        <Typography variant="h6">Nová karta</Typography>
        <Tooltip title="Uložit">
          <span>
            <IconButton onClick={handleSave} color="primary" disabled={saving}>
              {saving ? <CircularProgress size={24} /> : <SaveIcon />}
            </IconButton>
          </span>
        </Tooltip>
      </div>

      <div
        style={{
          maxHeight: "calc(100vh - 180px)",
          overflowY: "auto",
          paddingRight: 8,
          paddingTop: 8,
        }}
      >
        <Grid container spacing={2} sx={{ mt: 2 }}>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Název"
              required
              fullWidth
              value={card.name ?? ""}
              onChange={(e) => handleChange("name", e.target.value)}
              error={!card?.name}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <PriceInput
              label="Cena"
              value={card.price ?? null}
              onChange={(val) => handleChange("price", val)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Počet kusů"
              type="number"
              fullWidth
              value={card.quantity ?? ""}
              onChange={(e) =>
                handleChange("quantity", parseFloat(e.target.value))
              }
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Sériové číslo"
              fullWidth
              value={card.serialNumber ?? ""}
              onChange={(e) => handleChange("serialNumber", e.target.value)}
            />
          </Grid>

          <Grid size={{ xs: 12 }} sx={{ my: 2 }} />

          <Grid size={{ xs: 6 }}>
            <TextField
              label="Umístění"
              select
              required
              fullWidth
              value={card.locationId ?? ""}
              onChange={(e) => handleChange("locationId", e.target.value)}
              error={!card?.locationId}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const loc = locations.find((l) => l.id === value);
                    return `${value} - ${loc?.name ?? ""}`;
                  },
                },
              }}
            >
              {locations.map((l) => (
                <MenuItem key={l.id} value={l.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{l.id}</span>
                    <span>{l.name}</span>
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
              value={card.classId ?? ""}
              onChange={(e) => handleChange("classId", e.target.value)}
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
              required
              error={!card?.groupId}
              value={card.groupId ?? ""}
              onChange={(e) => handleChange("groupId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const group = groups.find((g) => g.id === value);
                    return `${value} - ${group?.name ?? ""}`;
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
              value={card.purchaseMethodId ?? ""}
              onChange={(e) => handleChange("purchaseMethodId", e.target.value)}
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
              value={card.purchaseInvoice ?? ""}
              onChange={(e) => handleChange("purchaseInvoice", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Datum pořízení *"
              value={card.purchaseDate ?? null}
              onChange={(date) => handleChange("purchaseDate", date)}
              slotProps={{
                textField: { fullWidth: true, error: !card?.purchaseDate },
              }}
            />
          </Grid>

          <Grid size={{ xs: 12 }} sx={{ my: 2 }} />

          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Datum vyřazení"
              value={card.writeOffDate ?? null}
              onChange={(date) => handleChange("writeOffDate", date)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Způsob vyřazení"
              select
              fullWidth
              value={card.writeOffMethodId ?? ""}
              onChange={(e) => handleChange("writeOffMethodId", e.target.value)}
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
              value={card.responsiblePersonId ?? ""}
              onChange={(e) =>
                handleChange("responsiblePersonId", e.target.value)
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
              multiline
              fullWidth
              value={card.note ?? ""}
              onChange={(e) => handleChange("note", e.target.value)}
            />
          </Grid>
        </Grid>
      </div>
    </div>
  );
};

export default CardCreateView;
