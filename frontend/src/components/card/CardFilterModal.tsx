/** Advanced card filter dialog. Allows filtering by ID range, name, price, date ranges, and all dictionary references. */
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Grid,
  Box,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers";
import { useMemo, useState } from "react";
import { CardFilterQuery } from "../../types/card/cardFilterQuery";
import { PriceInput } from "./PriceInput";
import { useLocations } from "../../hooks/queries/useLocationQueries";
import { useClasses } from "../../hooks/queries/useClassQueries";
import { useAssetGroups } from "../../hooks/queries/useAssetGroupQueries";
import { usePurchaseMethods } from "../../hooks/queries/usePurchaseMethodQueries";
import { useWriteOffMethods } from "../../hooks/queries/useWriteOffMethodQueries";
import { useUsers } from "../../hooks/queries/useCardQueries";
import { sortById } from "../../utils/czech";

interface Props {
  open: boolean;
  onClose: () => void;
  onApply: (filters: CardFilterQuery) => void;
}

const CardFilterModal = ({ open, onClose, onApply }: Props) => {
  const [filters, setFilters] = useState<CardFilterQuery>({});

  // Reuse TanStack Query cached data — no duplicate fetch
  const { data: rawLocations = [] } = useLocations();
  const { data: rawClasses = [] } = useClasses();
  const { data: rawGroups = [] } = useAssetGroups();
  const { data: rawPurchaseMethods = [] } = usePurchaseMethods();
  const { data: rawWriteOffMethods = [] } = useWriteOffMethods();
  const { data: rawUsers = [] } = useUsers();

  const locations = useMemo(() => sortById(rawLocations), [rawLocations]);
  const classes = useMemo(() => sortById(rawClasses), [rawClasses]);
  const groups = useMemo(() => sortById(rawGroups), [rawGroups]);
  const purchaseMethods = useMemo(() => sortById(rawPurchaseMethods), [rawPurchaseMethods]);
  const writeOffMethods = useMemo(() => sortById(rawWriteOffMethods), [rawWriteOffMethods]);
  const users = useMemo(() => sortById(rawUsers), [rawUsers]);

  const handleChange = (field: keyof CardFilterQuery, value: CardFilterQuery[keyof CardFilterQuery] | null) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value === "" ? undefined : value,
    }));
  };

  const handleReset = () => setFilters({});
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Pokročilé filtrování</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="ID"
              type="number"
              fullWidth
              value={filters.id ?? ""}
              onChange={(e) => handleChange("id", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Název"
              fullWidth
              value={filters.name ?? ""}
              onChange={(e) => handleChange("name", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Min ID"
              type="number"
              fullWidth
              value={filters.minId ?? ""}
              onChange={(e) => handleChange("minId", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Max ID"
              type="number"
              fullWidth
              value={filters.maxId ?? ""}
              onChange={(e) => handleChange("maxId", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <PriceInput
              label="Min. cena"
              value={filters.minPrice ?? null}
              onChange={(val) => handleChange("minPrice", val)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <PriceInput
              label="Max. cena"
              value={filters.maxPrice ?? null}
              onChange={(val) => handleChange("maxPrice", val)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Min. množství"
              type="number"
              fullWidth
              value={filters.minQuantity ?? ""}
              onChange={(e) => handleChange("minQuantity", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Max. množství"
              type="number"
              fullWidth
              value={filters.maxQuantity ?? ""}
              onChange={(e) => handleChange("maxQuantity", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Umístění"
              select
              fullWidth
              value={filters.locationId ?? ""}
              onChange={(e) => handleChange("locationId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}>
              <MenuItem value="">(nevybráno)</MenuItem>
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
              value={filters.classId ?? ""}
              onChange={(e) => handleChange("classId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}>
              <MenuItem value="">(nevybráno)</MenuItem>
              {classes.map((cls) => (
                <MenuItem key={cls.id} value={cls.id}>
                  <Box display="flex" gap={2} width="100%">
                    <span style={{ minWidth: 60 }}>{cls.id}</span>
                    <span>{cls.name}</span>
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Pořízeno po"
              value={filters.purchasedAfter ?? null}
              onChange={(value) => handleChange("purchasedAfter", value)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Pořízeno před"
              value={filters.purchasedBefore ?? null}
              onChange={(value) => handleChange("purchasedBefore", value)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Skupina"
              select
              fullWidth
              value={filters.groupId ?? ""}
              onChange={(e) => handleChange("groupId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}>
              <MenuItem value="">(nevybráno)</MenuItem>
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
              label="Faktura"
              fullWidth
              value={filters.purchaseInvoice ?? ""}
              onChange={(e) => handleChange("purchaseInvoice", e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Vyřazeno po"
              value={filters.writeOffAfter ?? null}
              onChange={(value) => handleChange("writeOffAfter", value)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Vyřazeno před"
              value={filters.writeOffBefore ?? null}
              onChange={(value) => handleChange("writeOffBefore", value)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Způsob pořízení"
              select
              fullWidth
              value={filters.purchaseMethodId ?? ""}
              onChange={(e) => handleChange("purchaseMethodId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}>
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
              label="Způsob vyřazení"
              select
              fullWidth
              value={filters.writeOffMethodId ?? ""}
              onChange={(e) => handleChange("writeOffMethodId", e.target.value)}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const selected = value as string;
                    return selected || "(nevybráno)";
                  },
                },
              }}>
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
            <DatePicker
              label="Vytvořeno po"
              value={filters.createdAfter ?? null}
              onChange={(value) => handleChange("createdAfter", value)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <DatePicker
              label="Vytvořeno před"
              value={filters.createdBefore ?? null}
              onChange={(value) => handleChange("createdBefore", value)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Vytvořil (uživatel)"
              select
              fullWidth
              value={filters.createdBy ?? ""}
              onChange={(e) => handleChange("createdBy", e.target.value)}>
              <MenuItem value="">(nevybráno)</MenuItem>
              {users.map((u) => (
                <MenuItem key={u.id} value={u.id}>
                  {u.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Zodpovědná osoba"
              select
              fullWidth
              value={filters.responsePerson ?? ""}
              onChange={(e) => handleChange("responsePerson", e.target.value)}>
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
              value={filters.note ?? ""}
              onChange={(e) => handleChange("note", e.target.value)}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleReset}>Reset</Button>
        <Button onClick={onClose}>Zavřít</Button>
        <Button
          variant="contained"
          onClick={() => {
            onApply(filters);
            onClose();
          }}>
          Filtrovat
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CardFilterModal;
