/**
 * Inventory differences page (DataGrid).
 * Compares expected vs. actual locations, lets users choose actions
 * (KEEP / SU / OU / WRITE_OFF) per card, and project changes to the database.
 */
import { useMemo, useState } from "react";
import {
  Box,
  Button,
  CircularProgress,
  CssBaseline,
  MenuItem,
  Select,
  TextField,
  ThemeProvider,
  Typography,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers";
import MenuBar from "../../components/MenuBar";
import { defaultTheme } from "../../theme";
import ProjectionConfirmDialog from "../../components/inventoryAudit/ProjectionConfirmDialog";
import {
  InventoryDifferenceEntry,
  InventoryDifferenceAction,
  InventoryDifferenceStatus,
} from "../../types/inventoryAudit/inventoryDifference";
import { translateError } from "../../utils/errorTranslations";
import { extractErrorMessage } from "../../utils/extractErrorMessage";

import { DataGrid, GridColDef, GridRenderCellParams } from "@mui/x-data-grid";
import { csCZ as dataGridCsCZ } from "@mui/x-data-grid/locales";
import { useNavigate } from "react-router-dom";

import { useDifferenceEntries } from "../../hooks/useDifferenceEntries";
import { useNotification } from "../../hooks/useNotification";

const statusTranslation: Record<InventoryDifferenceStatus, string> = {
  CORRECT: "Správně",
  WARN: "Upozornění",
  WRONG: "Chybně",
};

const rowColor: Record<InventoryDifferenceStatus, string> = {
  CORRECT: "rgba(0,128,0,0.08)",
  WARN: "rgba(255,165,0,0.12)",
  WRONG: "rgba(255,0,0,0.12)",
};

const InventoryDifferencesPage = () => {
  const [statusFilter, setStatusFilter] = useState<InventoryDifferenceStatus | "ALL">("ALL");
  const [confirmOpen, setConfirmOpen] = useState(false);

  const navigate = useNavigate();
  const { notify, notifyError, NotificationBar } = useNotification();

  // --- Core state managed by custom hook ---
  const {
    entries,
    loading,
    noRecords,
    writeOffMethods,
    updateEntryAndSave,
    hasInvalid,
    writeOffCount,
    warnCount,
    wrongCount,
    projectMutation,
  } = useDifferenceEntries(notifyError);

  const filtered = useMemo(() => {
    return entries.filter((e) => statusFilter === "ALL" || e.status === statusFilter);
  }, [entries, statusFilter]);

  const rows = filtered.map((e) => ({
    id: e.cardId,
    cardId: e.cardId,
    cardName: e.cardName,
    realLocation: e.realLocationId ? `${e.realLocationId} ${e.realLocationName ? "- " + e.realLocationName : ""}` : "",
    expectedLocation: e.expectedLocationId
      ? `${e.expectedLocationId} ${e.expectedLocationName ? "- " + e.expectedLocationName : ""}`
      : "",
    status: e.status,
    action: e.action ?? (e.status === "CORRECT" ? "KEEP" : e.status === "WARN" ? "SU" : ""),
    writeOffDate: e.writeOffDate ?? null,
    writeOffMethodId: e.writeOffMethodId ?? "",
  }));

  const selectableActions = (status: InventoryDifferenceStatus): Exclude<InventoryDifferenceAction, null>[] => {
    if (status === "CORRECT") return ["KEEP"];
    if (status === "WARN") return ["SU", "OU", "WRITE_OFF"];
    return ["OU", "WRITE_OFF"]; // WRONG
  };

  const actionLabel: Record<Exclude<InventoryDifferenceAction, null>, string> = {
    KEEP: "Nechat",
    SU: "Skutečné umístění",
    OU: "Očekávané umístění",
    WRITE_OFF: "Vyřadit",
  };

  const handleProject = () => {
    projectMutation.mutate(undefined, {
      onSuccess: () => {
        notify("Promítnutí dokončeno. Budete přesměrováni na majetkové karty.", "success");
        setTimeout(() => navigate("/cards"), 2500);
      },
      onError: (e: unknown) => {
        notifyError(translateError(extractErrorMessage(e)));
      },
      onSettled: () => setConfirmOpen(false),
    });
  };

  const columns: GridColDef[] = [
    { field: "cardId", headerName: "ID karty", width: 120 },
    { field: "cardName", headerName: "Název", flex: 1, minWidth: 180 },
    {
      field: "realLocation",
      headerName: "Skutečná lokace",
      flex: 1,
      minWidth: 200,
    },
    {
      field: "expectedLocation",
      headerName: "Očekávaná lokace",
      flex: 1,
      minWidth: 200,
    },
    {
      field: "status",
      headerName: "Status",
      width: 130,
      sortable: true,
      type: "singleSelect",
      valueOptions: [
        { value: "CORRECT", label: "Správně" },
        { value: "WARN", label: "Upozornění" },
        { value: "WRONG", label: "Chybně" },
      ],
      renderCell: (params: GridRenderCellParams) => statusTranslation[(params.row as InventoryDifferenceEntry).status],
      sortComparator: (v1, v2) =>
        statusTranslation[v1 as InventoryDifferenceStatus].localeCompare(
          statusTranslation[v2 as InventoryDifferenceStatus],
          "cs",
        ),
    },
    {
      field: "action",
      headerName: "Akce",
      width: 220,
      sortable: false,
      type: "singleSelect",
      valueOptions: [
        { value: "KEEP", label: "Nechat" },
        { value: "SU", label: "Skutečné umístění" },
        { value: "OU", label: "Očekávané umístění" },
        { value: "WRITE_OFF", label: "Vyřadit" },
      ],
      renderCell: (params) => {
        const row = params.row as InventoryDifferenceEntry;
        const entry = entries.find((x) => x.cardId === row.cardId)!;
        if (entry.status === "CORRECT") {
          return (
            <Select size="small" fullWidth value="KEEP" disabled>
              <MenuItem value="KEEP">Nechat</MenuItem>
            </Select>
          );
        }
        const actions = selectableActions(entry.status);
        return (
          <Select
            size="small"
            fullWidth
            value={entry.action ?? ""}
            displayEmpty={entry.status === "WRONG"}
            onChange={(ev) => {
              const val = (ev.target.value || null) as InventoryDifferenceAction;
              const patch: Partial<InventoryDifferenceEntry> = { action: val };
              if (val !== "WRITE_OFF") {
                patch.writeOffDate = null;
                patch.writeOffMethodId = null;
              }
              updateEntryAndSave(entry.cardId, patch);
            }}
            renderValue={(value) =>
              !value && entry.status === "WRONG"
                ? "(zvolit)"
                : actionLabel[value as Exclude<InventoryDifferenceAction, null>]
            }>
            {entry.status === "WRONG" && <MenuItem value="">(zvolit)</MenuItem>}
            {actions.map((a) => (
              <MenuItem key={a} value={a}>
                {actionLabel[a]}
              </MenuItem>
            ))}
          </Select>
        );
      },
    },
    {
      field: "writeOffDate",
      headerName: "Datum vyřazení",
      width: 220,
      sortable: false,
      renderCell: (params) => {
        const entry = entries.find((x) => x.cardId === (params.row as InventoryDifferenceEntry).cardId)!;
        if (entry.action !== "WRITE_OFF")
          return (
            <Box sx={{ height: "100%", display: "flex", alignItems: "center" }}>
              <span style={{ color: "#666" }}>—</span>
            </Box>
          );
        return (
          <Box sx={{ height: "100%", display: "flex", alignItems: "center" }}>
            <DatePicker
              label=""
              value={entry.writeOffDate ?? null}
              onChange={(date) => updateEntryAndSave(entry.cardId, { writeOffDate: date || null })}
              slotProps={{ textField: { size: "small", fullWidth: true } }}
            />
          </Box>
        );
      },
    },
    {
      field: "writeOffMethodId",
      headerName: "Způsob vyřazení",
      width: 220,
      sortable: false,
      renderCell: (params) => {
        const entry = entries.find((x) => x.cardId === (params.row as InventoryDifferenceEntry).cardId)!;
        if (entry.action !== "WRITE_OFF")
          return (
            <Box sx={{ height: "100%", display: "flex", alignItems: "center" }}>
              <span style={{ color: "#666" }}>—</span>
            </Box>
          );
        return (
          <Box sx={{ height: "100%", display: "flex", alignItems: "center" }}>
            <TextField
              select
              size="small"
              fullWidth
              label=""
              value={entry.writeOffMethodId ?? ""}
              onChange={(ev) =>
                updateEntryAndSave(entry.cardId, {
                  writeOffMethodId: (ev.target.value || null) as string | null,
                })
              }>
              {writeOffMethods.map((m) => (
                <MenuItem key={m.id} value={m.id}>
                  {m.id} - {m.name}
                </MenuItem>
              ))}
            </TextField>
          </Box>
        );
      },
    },
  ];

  return (
    <ThemeProvider theme={defaultTheme}>
      <CssBaseline />
      <div>
        <MenuBar />
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <Typography variant="h4">Inventura - Rozdíly</Typography>
            {!noRecords && (
              <Box display="flex" gap={2}>
                <TextField
                  select
                  label="Status"
                  size="small"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as InventoryDifferenceStatus | "ALL")}
                  style={{ minWidth: 180 }}>
                  <MenuItem value="ALL">Všechny</MenuItem>
                  <MenuItem value="CORRECT">Správně</MenuItem>
                  <MenuItem value="WARN">Upozornění</MenuItem>
                  <MenuItem value="WRONG">Chybně</MenuItem>
                </TextField>
                <Button
                  variant="contained"
                  color="primary"
                  disabled={loading || hasInvalid || projectMutation.isPending}
                  onClick={() => setConfirmOpen(true)}>
                  {projectMutation.isPending ? "Probíhá..." : "Promítnout"}
                </Button>
              </Box>
            )}
          </div>

          {NotificationBar}

          {loading ? (
            <div className="flex justify-center items-center h-64">
              <CircularProgress />
            </div>
          ) : noRecords ? (
            <Box className="mt-8" display="flex" flexDirection="column" alignItems="center" gap={2}>
              <Typography variant="h6">Zatím nejsou k dispozici žádné záznamy inventury.</Typography>
              <Typography variant="body2" color="text.secondary">
                Jakmile budou naskenovány položky, objeví se zde rozdíly k vyhodnocení.
              </Typography>
            </Box>
          ) : (
            <div style={{ height: "calc(100vh - 170px)", width: "100%" }}>
              <DataGrid
                rows={rows}
                columns={columns}
                disableRowSelectionOnClick
                sortingOrder={["asc", "desc"]}
                getRowClassName={(params) => `diff-row-${params.row.status.toLowerCase()}`}
                localeText={{
                  ...dataGridCsCZ.components.MuiDataGrid.defaultProps.localeText,
                  noRowsLabel: "Žádné záznamy",
                  paginationRowsPerPage: "Řádků na stránku:",
                }}
                sx={{
                  "& .diff-row-correct": { backgroundColor: rowColor.CORRECT },
                  "& .diff-row-warn": { backgroundColor: rowColor.WARN },
                  "& .diff-row-wrong": { backgroundColor: rowColor.WRONG },
                }}
              />
            </div>
          )}
          <ProjectionConfirmDialog
            open={confirmOpen}
            onClose={() => setConfirmOpen(false)}
            onConfirm={handleProject}
            totalCount={entries.length}
            writeOffCount={writeOffCount}
            warningCount={warnCount}
            wrongCount={wrongCount}
          />
        </div>
      </div>
    </ThemeProvider>
  );
};

export default InventoryDifferencesPage;
