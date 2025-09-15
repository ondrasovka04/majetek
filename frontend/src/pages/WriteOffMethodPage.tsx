import { useEffect, useState } from "react";
import {
  Typography,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Alert,
  CircularProgress,
  Collapse,
  ThemeProvider,
  CssBaseline,
} from "@mui/material";
import MenuBar from "../components/MenuBar";
import { WriteOffMethod } from "../types/writeOffMethod";
import { getWriteOffMethods } from "../services/writeOffMethodService";
import WriteOffMethodRow from "../components/writeOffMethod/WriteOffMethodRow";
import AddWriteOffMethodDialog from "../components/writeOffMethod/AddWriteOffMethodDialog";
import { translateError } from "../utils/errorTranslations";
import { dictionaryTheme } from "../theme";

const WriteOffMethodPage = () => {
  const [methods, setMethods] = useState<WriteOffMethod[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [error, setError] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [sortConfig, setSortConfig] = useState<{
    key: keyof WriteOffMethod;
    direction: "asc" | "desc";
  }>({ key: "id", direction: "asc" });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getWriteOffMethods();
        setMethods(data);
      } catch (error: any) {
        const backandMessage = error.response?.data?.message;
        handleError(translateError(backandMessage));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const collator = new Intl.Collator("cs", {
    sensitivity: "variant",
    usage: "sort",
    caseFirst: "false",
  });

  const sorted = [...methods].sort((a, b) => {
    const { key, direction } = sortConfig;
    const aVal = (a[key] ?? "").toString();
    const bVal = (b[key] ?? "").toString();
    if (key === "name") {
      if (!aVal.trim()) return 1;
      if (!bVal.trim()) return -1;
      return direction === "asc"
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
    }
    return direction === "asc"
      ? aVal.localeCompare(bVal, "cs", { numeric: true })
      : bVal.localeCompare(aVal, "cs", { numeric: true });
  });

  const handleSort = (key: keyof WriteOffMethod) => {
    setSortConfig((prev) =>
      prev.key === key
        ? { key, direction: prev.direction === "asc" ? "desc" : "asc" }
        : { key, direction: "asc" }
    );
  };

  const handleCreated = (item: WriteOffMethod) =>
    setMethods((prev) => [...prev, item]);

  const handleError = (msg: string) => {
    setError(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  return (
    <ThemeProvider theme={dictionaryTheme}>
      <CssBaseline />
      <div>
        <MenuBar />
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <Typography variant="h4">Způsob vyřazení</Typography>
            <Button variant="contained" onClick={() => setDialogOpen(true)}>
              Přidat
            </Button>
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
            <>
              <div
                style={{ maxHeight: "calc(100vh - 170px)", overflow: "auto" }}
              >
                <Table stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell
                        onClick={() => handleSort("id")}
                        style={{ cursor: "pointer", fontWeight: "bold" }}
                      >
                        ID{" "}
                        {sortConfig.key === "id" &&
                          (sortConfig.direction === "asc" ? "▲" : "▼")}
                      </TableCell>
                      <TableCell
                        onClick={() => handleSort("name")}
                        style={{ cursor: "pointer", fontWeight: "bold" }}
                      >
                        Název{" "}
                        {sortConfig.key === "name" &&
                          (sortConfig.direction === "asc" ? "▲" : "▼")}
                      </TableCell>
                      <TableCell>Akce</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {sorted.map((item) => (
                      <WriteOffMethodRow
                        key={item.id}
                        method={item}
                        onDelete={(id) =>
                          setMethods((prev) => prev.filter((m) => m.id !== id))
                        }
                        onUpdate={(id, name) =>
                          setMethods((prev) =>
                            prev.map((m) => (m.id === id ? { ...m, name } : m))
                          )
                        }
                        onError={handleError}
                      />
                    ))}
                  </TableBody>
                </Table>
              </div>

              <AddWriteOffMethodDialog
                open={dialogOpen}
                onClose={() => setDialogOpen(false)}
                onCreated={handleCreated}
              />
            </>
          )}
        </div>
      </div>
    </ThemeProvider>
  );
};

export default WriteOffMethodPage;
