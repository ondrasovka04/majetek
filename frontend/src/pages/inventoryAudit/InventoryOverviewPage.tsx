/** Inventory audit overview — sortable table of scanned cards with real location and timestamp. */
import { useEffect, useMemo, useState } from "react";
import {
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  CircularProgress,
  ThemeProvider,
  CssBaseline,
} from "@mui/material";
import MenuBar from "../../components/MenuBar";
import { InventoryAuditEntry } from "../../types/inventoryAudit/inventoryAudit";
import { defaultTheme } from "../../theme";
import { useInventoryAudit } from "../../hooks/queries/useInventoryAuditQueries";
import { useNotification } from "../../hooks/useNotification";
import { translateError } from "../../utils/errorTranslations";
import { extractErrorMessage } from "../../utils/extractErrorMessage";
import SortableHeaderCell from "../../components/inventoryAudit/SortableHeaderCell";

const InventoryOverviewPage = () => {
  const { data: entries = [], isLoading, error } = useInventoryAudit();
  const { notifyError, NotificationBar } = useNotification();
  const [sortBy, setSortBy] = useState<keyof InventoryAuditEntry>("cardId");
  const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");

  useEffect(() => {
    if (error) {
      notifyError(translateError(extractErrorMessage(error)));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [error]);

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
      setSortDirection((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortBy(column);
      setSortDirection("asc");
    }
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

          {NotificationBar}

          {isLoading ? (
            <div className="flex justify-center items-center h-64">
              <CircularProgress />
            </div>
          ) : (
            <div style={{ maxHeight: "calc(100vh - 170px)", overflow: "auto" }}>
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    <SortableHeaderCell
                      label="ID karty"
                      sortKey="cardId"
                      activeSortKey={sortBy}
                      sortDirection={sortDirection}
                      onSort={toggleSort}
                      style={{ width: 140, whiteSpace: "nowrap" }}
                    />
                    <SortableHeaderCell
                      label="Název karty"
                      sortKey="cardName"
                      activeSortKey={sortBy}
                      sortDirection={sortDirection}
                      onSort={toggleSort}
                      style={{ minWidth: 200 }}
                    />
                    <SortableHeaderCell
                      label="Skutečné umístění"
                      sortKey="realLocationId"
                      activeSortKey={sortBy}
                      sortDirection={sortDirection}
                      onSort={toggleSort}
                      style={{ minWidth: 240 }}
                    />
                    <SortableHeaderCell
                      label="Čas"
                      sortKey="timestamp"
                      activeSortKey={sortBy}
                      sortDirection={sortDirection}
                      onSort={toggleSort}
                      style={{ minWidth: 190 }}
                    />
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sortedEntries.map((e, idx) => (
                    <TableRow key={idx}>
                      <TableCell>{e.cardId}</TableCell>
                      <TableCell>{e.cardName}</TableCell>
                      <TableCell>{`${e.realLocationId} - ${e.realLocationName}`}</TableCell>
                      <TableCell>
                        {e.timestamp
                          .toLocaleString("cs-CZ", {
                            year: "numeric",
                            month: "2-digit",
                            day: "2-digit",
                            hour: "2-digit",
                            minute: "2-digit",
                            second: "2-digit",
                          })
                          .replace(/\.\s/g, ".")}
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
