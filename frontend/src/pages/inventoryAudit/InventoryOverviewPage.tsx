import { useEffect, useMemo, useState } from "react";
import {
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  CircularProgress,
  Alert,
  Collapse,
  ThemeProvider,
  CssBaseline,
} from "@mui/material";
import MenuBar from "../../components/MenuBar";
import { getInventoryAudit } from "../../services/inventoryAuditService";
import { InventoryAuditEntry } from "../../types/inventoryAudit/inventoryAudit";
import { translateError } from "../../utils/errorTranslations";
import { defaultTheme } from "../../theme";

const InventoryOverviewPage = () => {
  const [entries, setEntries] = useState<InventoryAuditEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false);
  const [sortBy, setSortBy] = useState<keyof InventoryAuditEntry>("cardId");
  const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");

  const sortedEntries = useMemo(() => {
    const copy = [...entries];
    copy.sort((a, b) => {
      const dir = sortDirection === "asc" ? 1 : -1;
      switch (sortBy) {
        case "cardId":
          return (a.cardId - b.cardId) * dir;
        case "cardName":
          return a.cardName.localeCompare(b.cardName, "cs") * dir;
        case "realLocationId":
          return a.realLocationId.localeCompare(b.realLocationId, "cs") * dir;
        case "realLocationName":
          return a.realLocationName.localeCompare(b.realLocationName, "cs") * dir;
        case "timestamp":
        default:
          return (a.timestamp.getTime() - b.timestamp.getTime()) * dir;
      }
    });
    return copy;
  }, [entries, sortBy, sortDirection]);

  const toggleSort = (column: keyof InventoryAuditEntry) => {
    if (sortBy === column) {
      setSortDirection(prev => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortBy(column);
      setSortDirection("asc");
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getInventoryAudit();
        setEntries(data);
      } catch (error: any) {
        const backendMessage = error.response?.data?.message;
        handleError(translateError(backendMessage));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleError = (msg: string) => {
    setError(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  return (
    <ThemeProvider theme={defaultTheme}>
      <CssBaseline />
      <div>
        <MenuBar />
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <Typography variant="h4">Inventura - Přehled</Typography>
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
          ) : (
            <div style={{ maxHeight: "calc(100vh - 170px)", overflow: "auto" }}>
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell style={{ fontWeight: "bold", cursor: "pointer", width: 140, whiteSpace: "nowrap" }} onClick={() => toggleSort("cardId")}> 
                      <span style={{ display: "inline-flex", alignItems: "center" }}>
                        <span>ID karty</span>
                        <span style={{ display: "inline-block", width: 14, marginLeft: 4 }}>{sortBy === "cardId" ? (sortDirection === "asc" ? "▲" : "▼") : " "}</span>
                      </span>
                    </TableCell>
                    <TableCell style={{ fontWeight: "bold", cursor: "pointer", minWidth: 200 }} onClick={() => toggleSort("cardName")}> 
                      <span style={{ display: "inline-flex", alignItems: "center" }}>
                        <span>Název karty</span>
                        <span style={{ display: "inline-block", width: 14, marginLeft: 4 }}>{sortBy === "cardName" ? (sortDirection === "asc" ? "▲" : "▼") : " "}</span>
                      </span>
                    </TableCell>
                    <TableCell style={{ fontWeight: "bold", cursor: "pointer", minWidth: 240 }} onClick={() => toggleSort("realLocationId")}> 
                      <span style={{ display: "inline-flex", alignItems: "center" }}>
                        <span>Skutečné umístění</span>
                        <span style={{ display: "inline-block", width: 14, marginLeft: 4 }}>{sortBy === "realLocationId" ? (sortDirection === "asc" ? "▲" : "▼") : " "}</span>
                      </span>
                    </TableCell>
                    <TableCell style={{ fontWeight: "bold", cursor: "pointer", minWidth: 190 }} onClick={() => toggleSort("timestamp")}> 
                      <span style={{ display: "inline-flex", alignItems: "center" }}>
                        <span>Čas</span>
                        <span style={{ display: "inline-block", width: 14, marginLeft: 4 }}>{sortBy === "timestamp" ? (sortDirection === "asc" ? "▲" : "▼") : " "}</span>
                      </span>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sortedEntries.map((e, idx) => (
                    <TableRow key={idx}>
                      <TableCell>{e.cardId}</TableCell>
                      <TableCell>{e.cardName}</TableCell>
                      <TableCell>{`${e.realLocationId} - ${e.realLocationName}`}</TableCell>
                      <TableCell>
                        {e.timestamp.toLocaleString("cs-CZ", {
                          year: "numeric",
                          month: "2-digit",
                          day: "2-digit",
                          hour: "2-digit",
                          minute: "2-digit",
                          second: "2-digit",
                        })}
                      </TableCell>
                    </TableRow>
                  ))}
                  {entries.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        Žádná data
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      </div>
    </ThemeProvider>
  );
};

export default InventoryOverviewPage;
