import { useEffect, useMemo, useState, useTransition } from "react";
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
  Collapse,
  Alert,
  IconButton,
  ToggleButton,
  Tooltip,
} from "@mui/material";
import MenuBar from "../components/MenuBar";
import { getCards, filterCards, printCards } from "../services/cardService";
import { getClasses } from "../services/classService";
import { getAssetGroups } from "../services/assetGroupService";
import { getLocations } from "../services/locationService";
import { getPurchaseMethods } from "../services/purchaseMethodService";
import { getWriteOffMethods } from "../services/writeOffMethodService";
import { getUsers } from "../services/authService";
import { Card } from "../types/card/card";
import CardRow from "../components/card/CardRow";
import { translateError } from "../utils/errorTranslations";
import Fuse from "fuse.js";
import FilterListIcon from "@mui/icons-material/FilterList";
import AddIcon from "@mui/icons-material/Add";
import CardFilterModal from "../components/card/CardFilterModal";
import { CardFilterQuery } from "../types/card/cardFilterQuery";
import PrintIcon from "@mui/icons-material/Print";
import CloudIcon from "@mui/icons-material/Cloud";
import ConfirmDialog from "../components/card/ConfirmDialog";
import CardDetailView from "../components/card/CardDetailView";
import CardCreateView from "../components/card/CardCreateView";
import { Class } from "../types/class";
import { AssetGroup } from "../types/assetGroup";
import { Location } from "../types/location";
import { PurchaseMethod } from "../types/purchaseMethod";
import { WriteOffMethod } from "../types/writeOffMethod";
import { User } from "../types/auth";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import { CardDetail } from "../types/card/cardDetail";
import { exportCardsExcel } from "../services/cardService";
import DownloadIcon from "@mui/icons-material/Download";

const normalizeString = (str: string) =>
  str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");

const CardPage = () => {
  const [cards, setCards] = useState<Card[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [classes, setClasses] = useState<Class[]>([]);
  const [groups, setGroups] = useState<AssetGroup[]>([]);
  const [purchaseMethods, setPurchaseMethods] = useState<PurchaseMethod[]>([]);
  const [writeOffMethods, setWriteOffMethods] = useState<WriteOffMethod[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [search, setSearch] = useState("");
  const [selectedCardId, setSelectedCardId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [filterOpen, setFilterOpen] = useState(false);
  const [cloudPrint, setCloudPrint] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [lastFilter, setLastFilter] = useState<CardFilterQuery>({});
  const [isPending, startTransition] = useTransition();
  const [showWrittenOff, setShowWrittenOff] = useState(false);
  const [duplicatedCard, setDuplicatedCard] =
    useState<Partial<CardDetail> | null>(null);
  const [sortConfig, setSortConfig] = useState<{
    key: keyof Card;
    direction: "asc" | "desc";
  }>({
    key: "id",
    direction: "asc",
  });

  useEffect(() => {
    const collator = new Intl.Collator("cs", {
      sensitivity: "variant",
      usage: "sort",
      caseFirst: "false",
    });
    const fetchData = async () => {
      try {
        const [cards, locs, cls, grps, pm, wom, usrs] = await Promise.all([
          getCards(),
          getLocations(),
          getClasses(),
          getAssetGroups(),
          getPurchaseMethods(),
          getWriteOffMethods(),
          getUsers(),
        ]);

        setLocations(locs.sort((a, b) => collator.compare(a.id, b.id)));
        setClasses(cls.sort((a, b) => collator.compare(a.id, b.id)));
        setGroups(grps.sort((a, b) => collator.compare(a.id, b.id)));
        setPurchaseMethods(pm.sort((a, b) => collator.compare(a.id, b.id)));
        setWriteOffMethods(wom.sort((a, b) => collator.compare(a.id, b.id)));
        setUsers(usrs.sort((a, b) => collator.compare(a.id, b.id)));

        setCards(cards);
      } catch (error: any) {
        const backandMessage = error.response?.data?.message;
        handleError(translateError(backandMessage));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleFilterApply = async (filters: CardFilterQuery) => {
    if (
      filters.purchasedBefore &&
      isNaN(new Date(filters.purchasedBefore).getTime())
    ) {
      handleError("Neplatné datum pořízení (před).");
      return;
    }

    if (
      filters.purchasedAfter &&
      isNaN(new Date(filters.purchasedAfter).getTime())
    ) {
      handleError("Neplatné datum pořízení (po).");
      return;
    }

    if (
      filters.writeOffBefore &&
      isNaN(new Date(filters.writeOffBefore).getTime())
    ) {
      handleError("Neplatné datum vyřazení (před).");
      return;
    }

    if (
      filters.writeOffAfter &&
      isNaN(new Date(filters.writeOffAfter).getTime())
    ) {
      handleError("Neplatné datum vyřazení (po).");
      return;
    }

    if (
      filters.createdBefore &&
      isNaN(new Date(filters.createdBefore).getTime())
    ) {
      handleError("Neplatné datum vytvoření (před).");
      return;
    }

    if (
      filters.createdAfter &&
      isNaN(new Date(filters.createdAfter).getTime())
    ) {
      handleError("Neplatné datum vytvoření (po).");
      return;
    }

    setLoading(true);
    setLastFilter(filters);
    try {
      const filtered = await filterCards(filters);
      setCards(filtered);
    } catch (error: any) {
      const backandMessage = error.response?.data?.message;
      handleError(translateError(backandMessage));
    } finally {
      setLoading(false);
    }
  };

  const collator = useMemo(
    () =>
      new Intl.Collator("cs", {
        sensitivity: "base",
        usage: "sort",
      }),
    []
  );

  const handleSort = (key: keyof Card) => {
    startTransition(() => {
      setSortConfig((prev) =>
        prev.key === key
          ? { key, direction: prev.direction === "asc" ? "desc" : "asc" }
          : { key, direction: "asc" }
      );
    });
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    startTransition(() => {
      setSearch(e.target.value);
    });
  };

  const handleError = (msg: string) => {
    setSuccessMessage("");
    setError(msg);
    setSnackbarOpen(true);

    setTimeout(() => {
      setSnackbarOpen(false);
    }, 6000);
  };
  const handleSuccess = (msg: string) => {
    setError("");
    setSuccessMessage(msg);
    setSnackbarOpen(true);

    setTimeout(() => {
      setSnackbarOpen(false);
    }, 6000);
  };

  const handlePrint = async () => {
    if (!sortedAndFilteredCards.length) {
      return handleError("Žádné karty k tisku.");
    }
    if (cloudPrint && sortedAndFilteredCards.length > 20) {
      setConfirmOpen(true);
      return;
    }
    sendPrintRequest();
  };

  const sendPrintRequest = async () => {
    const fullQuery: CardFilterQuery = {
      ...lastFilter,
      fulltextSearch: search.trim() || undefined,
      includeWriteOff: showWrittenOff,
    };

    try {
      setLoading(true);
      const msg = await printCards(fullQuery, cloudPrint);

      if (cloudPrint && msg) {
        handleSuccess(msg);
      }
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      handleError(translateError(backendMessage));
    } finally {
      setLoading(false);
      setConfirmOpen(false);
    }
  };

  const handleExcelExport = async () => {
    const fullQuery: CardFilterQuery = {
      ...lastFilter,
      fulltextSearch: search.trim() || undefined,
      includeWriteOff: showWrittenOff,
    };

    if (!sortedAndFilteredCards.length) {
      return handleError("Žádné karty k exportu.");
    }

    try {
      setLoading(true);
      await exportCardsExcel(fullQuery);
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      handleError(translateError(backendMessage));
    } finally {
      setLoading(false);
    }
  };

  const searchableCards = useMemo(() => {
    return cards.map((card) => ({
      ...card,
      normalizedName: normalizeString(card.name),
    }));
  }, [cards]);

  const fuse = useMemo(() => {
    const options = {
      keys: ["id", "normalizedName"],
      includeScore: false,
      minMatchCharLength: 1,
      ignoreLocation: true,
      threshold: 0.0,
    };
    return new Fuse(searchableCards, options);
  }, [searchableCards]);

  const filteredCards = useMemo(() => {
    let result = cards;

    if (!showWrittenOff) {
      result = result.filter((card) => !card.writeOffDate);
    }

    if (!search) return result;
    const normalizedSearch = normalizeString(search);
    const results = fuse.search(normalizedSearch);
    const matchingIds = new Set(results.map((result) => result.item.id));
    return result.filter((card) => matchingIds.has(card.id));
  }, [search, cards, fuse, showWrittenOff]);

  const sortedAndFilteredCards = useMemo(() => {
    return [...filteredCards].sort((a, b) => {
      const { key, direction } = sortConfig;

      const aVal = a[key as keyof Card] ?? "";
      const bVal = b[key as keyof Card] ?? "";

      if (key === "name") {
        const cmp = collator.compare(String(aVal), String(bVal));
        if (cmp !== 0) return direction === "asc" ? cmp : -cmp;
        return Number(a.id) - Number(b.id);
      }

      return direction === "asc"
        ? String(aVal).localeCompare(String(bVal), "cs", { numeric: true })
        : String(bVal).localeCompare(String(aVal), "cs", { numeric: true });
    });
  }, [filteredCards, sortConfig, collator]);

  return (
    <div className="min-h-screen bg-gray-50">
      <MenuBar />
      <div className="p-6 flex">
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
              sx={{ width: "100%" }}
            >
              {successMessage || error}
            </Alert>
          </Collapse>
        </div>
        <Paper className="w-1/3 mr-4 p-4" elevation={3}>
          <div>
            <div className="flex items-center justify-between">
              <Typography variant="h5">Karty</Typography>
              <div className="flex items-center space-x-2">
                <Tooltip title="Nová karta">
                  <IconButton
                    aria-label="New"
                    size="small"
                    onClick={() => setSelectedCardId("NEW")}
                  >
                    <AddIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip
                  title={
                    showWrittenOff ? "Skrýt vyřazené" : "Zobrazit vyřazené"
                  }
                >
                  <IconButton
                    aria-label="Toggle vyřazené"
                    size="small"
                    onClick={() => setShowWrittenOff((prev) => !prev)}
                  >
                    <VisibilityOffIcon
                      fontSize="small"
                      color={showWrittenOff ? "primary" : "disabled"}
                    />
                  </IconButton>
                </Tooltip>
                <Tooltip
                  title={cloudPrint ? "Tisk do cloudu" : "Stáhnout jako soubor"}
                >
                  <ToggleButton
                    value="cloud"
                    selected={cloudPrint}
                    onChange={() => setCloudPrint(!cloudPrint)}
                    size="small"
                    sx={{ border: "none" }}
                  >
                    <CloudIcon color={cloudPrint ? "primary" : "disabled"} />
                  </ToggleButton>
                </Tooltip>
                <Tooltip title="Tisk štítků">
                  <IconButton
                    aria-label="Print"
                    size="small"
                    onClick={handlePrint}
                  >
                    <PrintIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Export do Excelu">
                  <IconButton
                    aria-label="Export Excel"
                    size="small"
                    onClick={handleExcelExport}
                  >
                    <DownloadIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Pokročilé filtrování" sx={{ ml: 2 }}>
                  <IconButton
                    aria-label="Filter"
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
                      onClick={() => setSelectedCardId(card.id)}
                    />
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </Paper>

        <Paper className="w-2/3 p-4" elevation={3}>
          {selectedCardId === "NEW" ? (
            <CardCreateView
              prefillCard={duplicatedCard ?? undefined}
              onCreated={() => {
                setDuplicatedCard(null);
                setSelectedCardId(null);
                handleFilterApply(lastFilter);
              }}
              onError={handleError}
              onSuccess={(msg: string) => handleSuccess(msg)}
              locations={locations}
              groups={groups}
              classes={classes}
              purchaseMethods={purchaseMethods}
              writeOffMethods={writeOffMethods}
              users={users}
            />
          ) : selectedCardId ? (
            <CardDetailView
              cardId={selectedCardId}
              onChange={(newCard: CardDetail) => {
                setCards((prev) =>
                  prev.map((c) =>
                    c.id === selectedCardId
                      ? {
                          ...c,
                          name: newCard.name,
                          writeOffDate: newCard.writeOffDate,
                        }
                      : c
                  )
                );
              }}
              onDeleted={() => {
                setCards((prev) =>
                  prev.filter((card) => card.id !== selectedCardId)
                );
                setSelectedCardId(null);
                handleSuccess("Karta byla úspěšně smazána.");
              }}
              onDuplicate={(duplicatedCard) => {
                setDuplicatedCard(duplicatedCard);
                setSelectedCardId("NEW");
              }}
              locations={locations}
              groups={groups}
              classes={classes}
              purchaseMethods={purchaseMethods}
              writeOffMethods={writeOffMethods}
              users={users}
            />
          ) : (
            <Typography variant="h6" color="textSecondary">
              Vyberte kartu vlevo pro zobrazení detailu
            </Typography>
          )}
        </Paper>
      </div>
      <CardFilterModal
        open={filterOpen}
        onClose={() => setFilterOpen(false)}
        onApply={handleFilterApply}
      />
      <ConfirmDialog
        open={confirmOpen}
        description={`Chystáte se vytisknout ${sortedAndFilteredCards.length} štítků. Opravdu chcete pokračovat?`}
        onCancel={() => setConfirmOpen(false)}
        onConfirm={() => sendPrintRequest()}
      />
    </div>
  );
};

export default CardPage;
