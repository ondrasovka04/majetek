import { useEffect, useState } from "react";
import {
  Typography,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Collapse,
  Alert,
  CircularProgress,
  ThemeProvider,
  CssBaseline,
} from "@mui/material";
import MenuBar from "../components/MenuBar";
import { getLocations } from "../services/locationService";
import { Location } from "../types/location";
import LocationDialog from "../components/location/AddLocationDialog";
import { translateError } from "../utils/errorTranslations";
import LocationRow from "../components/location/LocationRow";
import { dictionaryTheme } from "../theme";

const LocationPage = () => {
  const [locations, setLocations] = useState<Location[]>([]);
  const [dialogOpen, setDialogOpen] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [successMessage, setSuccessMessage] = useState<string>("");
  const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [sortConfig, setSortConfig] = useState<{
    key: keyof Location;
    direction: "asc" | "desc";
  }>({
    key: "id",
    direction: "asc",
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getLocations();
        setLocations(data);
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

  const sortedLocations = [...locations].sort((a, b) => {
    if (!sortConfig) return 0;
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

  const handleSort = (key: keyof Location) => {
    setSortConfig((prev) => {
      if (prev?.key === key) {
        return { key, direction: prev.direction === "asc" ? "desc" : "asc" };
      }
      return { key, direction: "asc" };
    });
  };

  const handleCreated = (loc: Location) => {
    setLocations((prev) => [...prev, loc]);
  };

  const handleError = (msg: string) => {
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
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  return (
    <ThemeProvider theme={dictionaryTheme}>
      <CssBaseline />
      <div>
        <MenuBar />
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <Typography variant="h4">Umístění</Typography>
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
                severity={successMessage ? "success" : "error"}
                variant="filled"
                onClose={() => setSnackbarOpen(false)}
                sx={{ mb: 2 }}
              >
                {successMessage || error}
              </Alert>
            </Collapse>
          </div>

          {loading ? (
            <div className="flex justify-center items-center h-64">
              <CircularProgress />
            </div>
          ) : (
            <>
              <div style={{ maxHeight: "calc(100vh - 170px)", overflow: "auto" }}>
                <Table stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell
                        onClick={() => handleSort("id")}
                        style={{ cursor: "pointer", fontWeight: "bold" }}
                      >
                        ID{" "}
                        {sortConfig?.key === "id" &&
                          (sortConfig.direction === "asc" ? "▲" : "▼")}
                      </TableCell>
                      <TableCell
                        onClick={() => handleSort("name")}
                        style={{ cursor: "pointer", fontWeight: "bold" }}
                      >
                        Název{" "}
                        {sortConfig?.key === "name" &&
                          (sortConfig.direction === "asc" ? "▲" : "▼")}
                      </TableCell>

                      <TableCell colSpan={2} style={{ fontWeight: "bold" }}>
                        Akce{" "}
                      </TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {sortedLocations.map((loc) => (
                      <LocationRow
                        key={loc.id}
                        location={loc}
                        onDelete={(id) =>
                          setLocations((prev) =>
                            prev.filter((l) => l.id !== id)
                          )
                        }
                        onUpdate={(id, newName) =>
                          setLocations((prev) =>
                            prev.map((l) =>
                              l.id === id ? { ...l, name: newName } : l
                            )
                          )
                        }
                        onError={handleError}
                        onSuccess={handleSuccess}
                      />
                    ))}
                  </TableBody>
                </Table>
              </div>

              <LocationDialog
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

export default LocationPage;
