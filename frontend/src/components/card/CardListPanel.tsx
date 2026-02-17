/**
 * Left panel of the card management page.
 * Self-contained: owns card list fetching, search (Fuse.js), sort, filter,
 * print-label and Excel-export actions.
 * Exposes a `refilter()` imperative handle so the parent can
 * re-apply the last server-side filter after card creation.
 */
import {
  forwardRef,
  useImperativeHandle,
  useState,
  useTransition,
} from "react";
import {
  Typography,
  TextField,
  CircularProgress,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  IconButton,
  ToggleButton,
  Tooltip,
} from "@mui/material";
import FilterListIcon from "@mui/icons-material/FilterList";
import AddIcon from "@mui/icons-material/Add";
import PrintIcon from "@mui/icons-material/Print";
import CloudIcon from "@mui/icons-material/Cloud";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import DownloadIcon from "@mui/icons-material/Download";
import { Card } from "../../types/card/card";
import { CardFilterQuery } from "../../types/card/cardFilterQuery";
import { translateError } from "../../utils/errorTranslations";
import { extractErrorMessage } from "../../utils/extractErrorMessage";
import {
  useCards,
  useFilterCards,
  usePrintCards,
  useExportCardsExcel,
} from "../../hooks/queries/useCardQueries";
import CardRow from "./CardRow";
import CardFilterModal from "./CardFilterModal";
import ConfirmDialog from "./ConfirmDialog";
import { useCardSearch } from "../../hooks/useCardSearch";

// --- Types ---

export interface CardListPanelHandle {
  /** Re-applies the last server-side filter (used after card creation). */
  refilter: () => void;
}

interface Props {
  selectedCardId: string | null;
  onSelectCard: (id: string | null) => void;
  onNewCard: () => void;
  onNotify: (msg: string, severity: "success" | "error") => void;
  onError: (msg: string) => void;
}

// --- Component ---

const CardListPanel = forwardRef<CardListPanelHandle, Props>(
  ({ selectedCardId, onSelectCard, onNewCard, onNotify, onError }, ref) => {
    // --- UI state ---
    const [search, setSearch] = useState("");
    const [filterOpen, setFilterOpen] = useState(false);
    const [cloudPrint, setCloudPrint] = useState(false);
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [lastFilter, setLastFilter] = useState<CardFilterQuery>({});
    const [isPending, startTransition] = useTransition();
    const [showWrittenOff, setShowWrittenOff] = useState(false);
    const [sortConfig, setSortConfig] = useState<{
      key: keyof Card;
      direction: "asc" | "desc";
    }>({ key: "id", direction: "asc" });

    // --- TanStack Query ---
    const { data: cards = [], isLoading: cardsLoading } = useCards();
    const filterMutation = useFilterCards();
    const printMutation = usePrintCards();
    const excelMutation = useExportCardsExcel();

    const loading =
      cardsLoading ||
      filterMutation.isPending ||
      printMutation.isPending ||
      excelMutation.isPending;

    // --- Imperative handle ---
    useImperativeHandle(ref, () => ({
      refilter: () => {
        if (Object.keys(lastFilter).length > 0) {
          filterMutation.mutate(lastFilter, {
            onError: (err: unknown) =>
              onError(translateError(extractErrorMessage(err))),
          });
        }
      },
    }));

    // --- Handlers ---
    const handleFilterApply = (filters: CardFilterQuery) => {
      const dateFields: (keyof CardFilterQuery)[] = [
        "purchasedBefore",
        "purchasedAfter",
        "writeOffBefore",
        "writeOffAfter",
        "createdBefore",
        "createdAfter",
      ];
      for (const field of dateFields) {
        const val = filters[field];
        if (val && isNaN(new Date(val as string | Date).getTime())) {
          onError(`Neplatné datum (${field}).`);
          return;
        }
      }

      setLastFilter(filters);
      filterMutation.mutate(filters, {
        onError: (err: unknown) =>
          onError(translateError(extractErrorMessage(err))),
      });
    };

    const handleSort = (key: keyof Card) => {
      startTransition(() => {
        setSortConfig((prev) =>
          prev.key === key
            ? { key, direction: prev.direction === "asc" ? "desc" : "asc" }
            : { key, direction: "asc" },
        );
      });
    };

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      startTransition(() => setSearch(e.target.value));
    };

    const buildFullQuery = (): CardFilterQuery => ({
      ...lastFilter,
      fulltextSearch: search.trim() || undefined,
      includeWriteOff: showWrittenOff,
    });

    const handlePrint = () => {
      if (!sortedAndFilteredCards.length) {
        return onError("Žádné karty k tisku.");
      }
      if (cloudPrint && sortedAndFilteredCards.length > 20) {
        setConfirmOpen(true);
        return;
      }
      sendPrintRequest();
    };

    const sendPrintRequest = () => {
      printMutation.mutate(
        { query: buildFullQuery(), cloudPrint },
        {
          onSuccess: (msg) => {
            if (cloudPrint && msg) onNotify(msg, "success");
          },
          onError: (err: unknown) =>
            onError(translateError(extractErrorMessage(err))),
          onSettled: () => setConfirmOpen(false),
        },
      );
    };

    const handleExcelExport = () => {
      if (!sortedAndFilteredCards.length) {
        return onError("Žádné karty k exportu.");
      }
      excelMutation.mutate(buildFullQuery(), {
        onError: (err: unknown) =>
          onError(translateError(extractErrorMessage(err))),
      });
    };

    // --- Search + sort (Fuse.js) ---
    const sortedAndFilteredCards = useCardSearch(cards, search, showWrittenOff, sortConfig);

    // --- Render ---
    return (
      <>
        <Paper className="w-1/3 mr-4 p-4" elevation={3}>
          <div>
            <div className="flex items-center justify-between">
              <Typography variant="h5">Karty</Typography>
              <div className="flex items-center space-x-2">
                <Tooltip title="Nová karta">
                  <IconButton size="small" onClick={onNewCard}>
                    <AddIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip
                  title={
                    showWrittenOff ? "Skrýt vyřazené" : "Zobrazit vyřazené"
                  }
                >
                  <IconButton
                    size="small"
                    onClick={() => setShowWrittenOff((p) => !p)}
                  >
                    <VisibilityOffIcon
                      fontSize="small"
                      color={showWrittenOff ? "primary" : "disabled"}
                    />
                  </IconButton>
                </Tooltip>
                <Tooltip
                  title={
                    cloudPrint ? "Tisk do cloudu" : "Stáhnout jako soubor"
                  }
                >
                  <ToggleButton
                    value="cloud"
                    selected={cloudPrint}
                    onChange={() => setCloudPrint(!cloudPrint)}
                    size="small"
                    sx={{ border: "none" }}
                  >
                    <CloudIcon
                      color={cloudPrint ? "primary" : "disabled"}
                    />
                  </ToggleButton>
                </Tooltip>
                <Tooltip title="Tisk štítků">
                  <IconButton size="small" onClick={handlePrint}>
                    <PrintIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Export do Excelu">
                  <IconButton size="small" onClick={handleExcelExport}>
                    <DownloadIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Pokročilé filtrování" sx={{ ml: 2 }}>
                  <IconButton
                    size="small"
                    onClick={() => setFilterOpen(true)}
                  >
                    <FilterListIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </div>
            </div>
            <div className="flex items-center justify-between">
              <TextField
                label="Vyhledat"
                autoComplete="off"
                defaultValue={search}
                onChange={handleSearchChange}
                margin="normal"
                fullWidth
              />
              <Typography
                variant="body2"
                sx={{
                  mt: 1,
                  ml: 1,
                  whiteSpace: "nowrap",
                  color:
                    sortedAndFilteredCards.length === 0
                      ? "error.main"
                      : "text.secondary",
                  fontSize: 15,
                }}
              >
                {!loading && sortedAndFilteredCards.length}
              </Typography>
            </div>
          </div>

          {loading ? (
            <div className="flex justify-center items-center h-64">
              <CircularProgress />
            </div>
          ) : (
            <div style={{ height: "calc(100vh - 265px)", overflowY: "auto" }}>
              <Table size="small" stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ width: "1px", p: 0 }} />
                    <TableCell
                      onClick={() => handleSort("id")}
                      style={{
                        whiteSpace: "nowrap",
                        cursor: "pointer",
                        fontWeight: "bold",
                      }}
                    >
                      ID{" "}
                      {sortConfig.key === "id" &&
                        (sortConfig.direction === "asc" ? "▲" : "▼")}
                    </TableCell>
                    <TableCell
                      onClick={() => handleSort("name")}
                      style={{
                        whiteSpace: "nowrap",
                        cursor: "pointer",
                        fontWeight: "bold",
                      }}
                    >
                      Název{" "}
                      {sortConfig.key === "name" &&
                        (sortConfig.direction === "asc" ? "▲" : "▼")}
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody
                  style={{
                    opacity: isPending ? 0.5 : 1,
                    transition: "opacity 300ms",
                  }}
                >
                  {sortedAndFilteredCards.map((card) => (
                    <CardRow
                      key={card.id}
                      card={card}
                      selected={card.id === selectedCardId}
                      onClick={() => onSelectCard(card.id)}
                    />
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </Paper>

        <CardFilterModal
          open={filterOpen}
          onClose={() => setFilterOpen(false)}
          onApply={handleFilterApply}
        />
        <ConfirmDialog
          open={confirmOpen}
          description={`Chystáte se vytisknout ${sortedAndFilteredCards.length} štítků. Opravdu chcete pokračovat?`}
          onCancel={() => setConfirmOpen(false)}
          onConfirm={sendPrintRequest}
        />
      </>
    );
  },
);

CardListPanel.displayName = "CardListPanel";

export default CardListPanel;
