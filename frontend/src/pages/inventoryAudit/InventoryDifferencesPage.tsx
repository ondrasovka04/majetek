import React, { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Collapse,
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
import {
  getInventoryDifferences,
  patchInventoryDifference,
  projectInventoryDifferences,
} from "../../services/inventoryAuditService";
import ProjectionConfirmDialog from "../../components/inventoryAudit/ProjectionConfirmDialog";
import {
  InventoryDifferenceEntry,
  InventoryDifferenceAction,
  InventoryDifferenceStatus,
} from "../../types/inventoryAudit/inventoryDifference";
import { getWriteOffMethods } from "../../services/writeOffMethodService";
import { WriteOffMethod } from "../../types/writeOffMethod";
import { translateError } from "../../utils/errorTranslations";

import { DataGrid, GridColDef, GridRenderCellParams } from "@mui/x-data-grid";
import { csCZ as dataGridCsCZ } from "@mui/x-data-grid/locales";
import { useNavigate } from "react-router-dom";

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
  const [entries, setEntries] = useState<InventoryDifferenceEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState<
    InventoryDifferenceStatus | "ALL"
  >("ALL");
  const [writeOffMethods, setWriteOffMethods] = useState<WriteOffMethod[]>([]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [projecting, setProjecting] = useState(false);
  const [projectSuccess, setProjectSuccess] = useState<string | null>(null);
  const navigate = useNavigate();
  const [noRecords, setNoRecords] = useState(false);

  useEffect(() => {
    let active = true;
    const fetchData = async () => {
      try {
        const [diffs, wom] = await Promise.all([
          getInventoryDifferences(),
          getWriteOffMethods(),
        ]);
        if (!active) return;
        const defaultWriteOffMethodId =
          (wom.find((m) => m.id === "A")?.id || wom[0]?.id) ?? null;

        const toInit: { cardId: number; action: InventoryDifferenceAction }[] =
          [];

        const mapped = diffs.map((d) => {
          const derivedAction =
            d.action ??
            (d.status === "CORRECT"
              ? "KEEP"
              : d.status === "WARN"
              ? "SU"
              : null);
          if (d.action == null && derivedAction) {
            toInit.push({ cardId: d.cardId, action: derivedAction });
          }
          return {
            ...d,
            action: derivedAction,
            writeOffMethodId:
              d.writeOffMethodId ??
              (derivedAction === "WRITE_OFF" ? defaultWriteOffMethodId : null),
          };
        });
        setEntries(mapped);
        setWriteOffMethods(wom);

        if (toInit.length > 0) {
          try {
            await Promise.all(
              toInit.map((i) =>
                patchInventoryDifference(i.cardId, {
                  action: i.action,
                  writeOffDate: null,
                  writeOffMethodId: null,
                })
              )
            );
          } catch (e: any) {
            handleError(
              translateError(e?.response?.data?.message || e.message)
            );
          }
        }
      } catch (e: any) {
        if (active) {
          const msg = e?.response?.data?.message || e.message || "";
          if (msg === "No inventory audit records found.") {
            setNoRecords(true);
            setLoading(false);
            return;
          }
          handleError(translateError(e.message));
        }
      } finally {
        if (active) setLoading(false);
      }
    };
    fetchData();
    return () => {
      active = false;
    };
  }, []);

  const handleError = (msg: string) => {
    setError(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  const filtered = useMemo(() => {
    return entries.filter(
      (e) => statusFilter === "ALL" || e.status === statusFilter
    );
  }, [entries, statusFilter]);

  const sorted = filtered;

  const updateEntryAndSave = async (
    cardId: number,
    patch: Partial<InventoryDifferenceEntry>
  ) => {
    const prevEntry = entries.find((e) => e.cardId === cardId);
    if (!prevEntry) return;

    const nextEntry: InventoryDifferenceEntry = {
      ...prevEntry,
      ...patch,
    } as InventoryDifferenceEntry;

    if (nextEntry.action === "WRITE_OFF" && !nextEntry.writeOffMethodId) {
      nextEntry.writeOffMethodId =
        (writeOffMethods.find((m) => m.id === "A")?.id ||
          writeOffMethods[0]?.id) ??
        null;
    }

    setEntries((prev) =>
      prev.map((e) => (e.cardId === cardId ? nextEntry : e))
    );

    const toIsoDate = (d: Date | string | null | undefined) => {
      if (!d) return null;
      if (d instanceof Date) return d.toISOString().split("T")[0];
      return d;
    };

    const payload: any = {};
    const actionChanged = patch.action !== undefined;
    const dateChanged = patch.writeOffDate !== undefined;
    const methodChanged = patch.writeOffMethodId !== undefined;

    // Logika tvorby payloadu:
    // 1) Pokud action změněna -> vždy poslat action. Pokud není WRITE_OFF -> vynulovat vyřazovací pole.
    // 2) Pokud (nextEntry je WRITE_OFF) a mění se datum nebo metoda -> poslat všechny tři hodnoty (action + datum + metoda).
    // 3) Pokud patch nemá action, ale nextEntry je WRITE_OFF a mění se datum/metoda -> poslat action (WRITE_OFF) + vyřazovací hodnoty.

    if (actionChanged) {
      payload.action = nextEntry.action ?? null;
      if (nextEntry.action === "WRITE_OFF") {
        payload.writeOffDate = toIsoDate(nextEntry.writeOffDate);
        payload.writeOffMethodId = nextEntry.writeOffMethodId ?? null;
      } else {
        payload.writeOffDate = null;
        payload.writeOffMethodId = null;
      }
    } else if (
      nextEntry.action === "WRITE_OFF" &&
      (dateChanged || methodChanged)
    ) {
      payload.action = "WRITE_OFF";
      payload.writeOffDate = toIsoDate(nextEntry.writeOffDate);
      payload.writeOffMethodId = nextEntry.writeOffMethodId ?? null;
    }

    if (Object.keys(payload).length === 0) return;

    try {
      await patchInventoryDifference(cardId, payload);
    } catch (err: any) {
      setEntries((prev) =>
        prev.map((e) => (e.cardId === cardId ? prevEntry : e))
      );
      handleError(translateError(err?.response?.data?.message || err.message));
    }
  };

  const selectableActions = (
    status: InventoryDifferenceStatus
  ): Exclude<InventoryDifferenceAction, null>[] => {
    if (status === "CORRECT") return ["KEEP"];
    if (status === "WARN") return ["SU", "OU", "WRITE_OFF"];
    return ["OU", "WRITE_OFF"]; // WRONG
  };

  const actionLabel: Record<
    Exclude<InventoryDifferenceAction, null>,
    string
  > = {
    KEEP: "Nechat",
    SU: "Skutečné umístění",
    OU: "Očekávané umístění",
    WRITE_OFF: "Vyřadit",
  };

  const rows = sorted.map((e) => ({
    id: e.cardId,
    cardId: e.cardId,
    cardName: e.cardName,
    realLocation: e.realLocationId
      ? `${e.realLocationId} ${
          e.realLocationName ? "- " + e.realLocationName : ""
        }`
      : "",
    expectedLocation: e.expectedLocationId
      ? `${e.expectedLocationId} ${
          e.expectedLocationName ? "- " + e.expectedLocationName : ""
        }`
      : "",
    status: e.status,
    action:
      e.action ??
      (e.status === "CORRECT" ? "KEEP" : e.status === "WARN" ? "SU" : ""),
    writeOffDate: e.writeOffDate ?? null,
    writeOffMethodId: e.writeOffMethodId ?? "",
  }));

  const hasInvalid = useMemo(() => {
    return entries.some((e) => {
      // WRONG bez akce (prázdný string v rows) => e.action null nebo ""
      if (e.status === "WRONG" && !e.action) return true;
      if (e.action === "WRITE_OFF" && (!e.writeOffDate || !e.writeOffMethodId))
        return true;
      return false;
    });
  }, [entries]);

  const writeOffCount = useMemo(
    () => entries.filter((e) => e.action === "WRITE_OFF").length,
    [entries]
  );
  const warnCount = useMemo(
    () => entries.filter((e) => e.status === "WARN").length,
    [entries]
  );
  const wrongCount = useMemo(
    () => entries.filter((e) => e.status === "WRONG").length,
    [entries]
  );

  const handleProject = async () => {
    setProjecting(true);
    try {
      await projectInventoryDifferences();
      setProjectSuccess(
        "Promítnutí dokončeno. Budete přesměrováni na majetkové karty."
      );
      setTimeout(() => navigate("/cards"), 2500);
    } catch (e: any) {
      handleError(translateError(e?.response?.data?.message || e.message));
    } finally {
      setProjecting(false);
      setConfirmOpen(false);
    }
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
      renderCell: (params: GridRenderCellParams) =>
        statusTranslation[
          (params.row as any).status as InventoryDifferenceStatus
        ],
      sortComparator: (v1, v2) =>
        statusTranslation[v1 as InventoryDifferenceStatus].localeCompare(
          statusTranslation[v2 as InventoryDifferenceStatus],
          "cs"
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
        const row = params.row as any;
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
              const val = (ev.target.value ||
                null) as InventoryDifferenceAction;
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
            }
          >
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
        const entry = entries.find(
          (x) => x.cardId === (params.row as any).cardId
        )!;
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
              onChange={(date) =>
                updateEntryAndSave(entry.cardId, { writeOffDate: date || null })
              }
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
        const entry = entries.find(
          (x) => x.cardId === (params.row as any).cardId
        )!;
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
              }
            >
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
                  onChange={(e) => setStatusFilter(e.target.value as any)}
                  style={{ minWidth: 180 }}
                >
                  <MenuItem value="ALL">Všechny</MenuItem>
                  <MenuItem value="CORRECT">Správně</MenuItem>
                  <MenuItem value="WARN">Upozornění</MenuItem>
                  <MenuItem value="WRONG">Chybně</MenuItem>
                </TextField>
                <Button
                  variant="contained"
                  color="primary"
                  disabled={loading || hasInvalid || projecting}
                  onClick={() => setConfirmOpen(true)}
                >
                  {projecting ? "Probíhá..." : "Promítnout"}
                </Button>
              </Box>
            )}
          </div>

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
                sx={{ width: "100%" }}
              >
                {error}
              </Alert>
            </Collapse>
          </div>

          {loading ? (
            <div className="flex justify-center items-center h-64">
              <CircularProgress />
            </div>
          ) : noRecords ? (
            <Box
              className="mt-8"
              display="flex"
              flexDirection="column"
              alignItems="center"
              gap={2}
            >
              <Typography variant="h6">
                Zatím nejsou k dispozici žádné záznamy inventury.
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Jakmile budou naskenovány položky, objeví se zde rozdíly k
                vyhodnocení.
              </Typography>
            </Box>
          ) : (
            <div style={{ height: "calc(100vh - 170px)", width: "100%" }}>
              <DataGrid
                rows={rows}
                columns={columns}
                disableRowSelectionOnClick
                sortingOrder={["asc", "desc"]}
                getRowClassName={(params) =>
                  `diff-row-${params.row.status.toLowerCase()}`
                }
                localeText={{
                  ...dataGridCsCZ.components.MuiDataGrid.defaultProps
                    .localeText,
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
          {projectSuccess && (
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
              <Collapse in={!!projectSuccess}>
                <Alert
                  severity="success"
                  variant="filled"
                  onClose={() => setProjectSuccess(null)}
                  sx={{ width: "100%" }}
                >
                  {projectSuccess}
                </Alert>
              </Collapse>
            </div>
          )}
        </div>
      </div>
    </ThemeProvider>
  );
};

export default InventoryDifferencesPage;
