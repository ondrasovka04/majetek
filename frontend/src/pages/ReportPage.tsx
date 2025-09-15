import { useState, useEffect } from "react";
import {
  Paper,
  Typography,
  List,
  ListItemButton,
  CircularProgress,
  Alert,
  Collapse,
} from "@mui/material";
import MenuBar from "../components/MenuBar";
import { AssetGroup } from "../types/assetGroup";
import { Location } from "../types/location";
import { getAssetGroups } from "../services/assetGroupService";
import { getLocations } from "../services/locationService";
import {
  exportCardPdf,
  exportGroupPdf,
  exportLocationPdf,
} from "../services/cardService";
import { translateError } from "../utils/errorTranslations";
import { Worker, Viewer } from "@react-pdf-viewer/core";
import "@react-pdf-viewer/core/lib/styles/index.css";
import "@react-pdf-viewer/default-layout/lib/styles/index.css";
import { defaultLayoutPlugin } from "@react-pdf-viewer/default-layout";
import GroupReportModal from "../components/report/GroupReportModal";
import czechLocalization from "../utils/czechLocalization";
import LocationReportModal from "../components/report/LocationReportModal";
import CardReportModal from "../components/report/CardReportModal";

type ActiveModal = "group" | "location" | "card" | null;

const ReportPage = () => {
  const [groups, setGroups] = useState<AssetGroup[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<string | null>(null);
  const [selectedLocation, setSelectedLocation] = useState<string | null>(null);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [activeModal, setActiveModal] = useState<ActiveModal>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(new Date());

  const defaultLayoutPluginInstance = defaultLayoutPlugin({
    renderToolbar: (Toolbar) => (
      <Toolbar>
        {(slots) => (
          <div
            style={{
              alignItems: "center",
              display: "flex",
              width: "100%",
            }}
          >
            <div
              style={{
                alignItems: "center",
                display: "flex",
              }}
            >
              <div className="mt-1 ml-1">
                <slots.ShowSearchPopover />
              </div>
              <div className="mt-1">
                <slots.GoToPreviousPage />
              </div>
              <div className="max-w-10 min-w-7 mr-3">
                <slots.CurrentPageInput />
              </div>
              <div className="mr-2">
                / <slots.NumberOfPages />
              </div>
              <div className="mt-1">
                <slots.GoToNextPage />
              </div>
            </div>
            <div
              style={{
                alignItems: "center",
                display: "flex",
                flexGrow: 1,
                flexShrink: 1,
                justifyContent: "center",
              }}
            >
              <div className="mt-1">
                <slots.ZoomOut />
              </div>
              <slots.Zoom />
              <div className="mt-1">
                <slots.ZoomIn />
              </div>
            </div>
            <div
              style={{
                alignItems: "center",
                display: "flex",
              }}
            >
              <div className="mt-1">
                <slots.EnterFullScreen />
              </div>
              <div className="mt-1">
                <slots.Download />
              </div>
              <div className="mt-1 mr-2">
                <slots.Print />
              </div>
            </div>
          </div>
        )}
      </Toolbar>
    ),
    sidebarTabs: () => [],
  });

  useEffect(() => {
    const collator = new Intl.Collator("cs", {
      sensitivity: "variant",
      usage: "sort",
      caseFirst: "false",
    });

    const fetchData = async () => {
      try {
        const [groupsData, locationsData] = await Promise.all([
          getAssetGroups(),
          getLocations(),
        ]);
        setGroups(groupsData.sort((a, b) => collator.compare(a.id, b.id)));
        setLocations(
          locationsData.sort((a, b) => collator.compare(a.id, b.id))
        );
      } catch (err: any) {
        const backendMessage = err.response?.data?.message;
        handleError(translateError(backendMessage));
      }
    };

    fetchData();
  }, []);

  const handleError = (msg: string) => {
    setError(msg);
    setSnackbarOpen(true);
    setTimeout(() => setSnackbarOpen(false), 6000);
  };

  const handleGenerateGroupPdf = async () => {
    if (selectedGroup === null) return;

    setActiveModal(null);

    try {
      setLoading(true);
      const objectUrl = await exportGroupPdf(selectedGroup, selectedDate);
      setPdfUrl(objectUrl);
      setSelectedDate(new Date());
    } catch (error: any) {
      const backendMessage = error.response?.data?.message;
      handleError(translateError(backendMessage));
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateLocationPdf = async () => {
    if (selectedLocation === null)
      return handleError("Musíte vybrat místnost.");
    setActiveModal(null);
    setLoading(true);

    try {
      const objectUrl = await exportLocationPdf(selectedLocation, selectedDate);
      setPdfUrl(objectUrl);
      setSelectedDate(new Date());
    } catch (error: any) {
      handleError(translateError(error.response?.data?.message));
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateCardPdf = async (cardId: number) => {
    setActiveModal(null);
    setLoading(true);
    try {
      const objectUrl = await exportCardPdf(cardId);
      setPdfUrl(objectUrl.url);
    } catch (error: any) {
      handleError(translateError(error.response?.data?.message));
    } finally {
      setLoading(false);
    }
  };

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
              severity="error"
              variant="filled"
              onClose={() => setSnackbarOpen(false)}
              sx={{ width: "100%" }}
            >
              {error}
            </Alert>
          </Collapse>
        </div>

        <Paper className="w-1/3 mr-4 p-4" elevation={3}>
          <Typography variant="h6" gutterBottom>
            Sestavy
          </Typography>
          <List>
            <ListItemButton onClick={() => setActiveModal("group")}>
              Podle skupin
            </ListItemButton>
            <ListItemButton onClick={() => setActiveModal("location")}>
              Podle místností
            </ListItemButton>
            <ListItemButton onClick={() => setActiveModal("card")}>
              Karta majetku
            </ListItemButton>
          </List>
        </Paper>

        <Paper
          className="p-4"
          elevation={3}
          style={{
            width: "66.6667%",
            height: pdfUrl ? "calc(100vh - 120px)" : "auto",
          }}
        >
          {loading ? (
            <div className="flex justify-center items-center h-full">
              <CircularProgress />
            </div>
          ) : pdfUrl ? (
            <Worker workerUrl="https://unpkg.com/pdfjs-dist@3.11.174/build/pdf.worker.min.js">
              <div style={{ height: "100%" }}>
                <Viewer
                  localization={czechLocalization}
                  fileUrl={pdfUrl}
                  plugins={[defaultLayoutPluginInstance]}
                  defaultScale={1.5}
                />
              </div>
            </Worker>
          ) : (
            <Typography variant="body1" color="textSecondary">
              Vyberte report vlevo pro zobrazení.
            </Typography>
          )}
        </Paper>
      </div>

      <GroupReportModal
        open={activeModal === "group"}
        onClose={() => setActiveModal(null)}
        onGenerate={handleGenerateGroupPdf}
        selectedGroup={selectedGroup}
        onGroupChange={setSelectedGroup}
        groups={groups}
        loading={loading}
        selectedDate={selectedDate}
        onDateChange={setSelectedDate}
      />

      <LocationReportModal
        open={activeModal === "location"}
        onClose={() => setActiveModal(null)}
        onGenerate={handleGenerateLocationPdf}
        selectedLocation={selectedLocation}
        onLocationChange={setSelectedLocation}
        locations={locations}
        loading={loading}
        selectedDate={selectedDate}
        onDateChange={setSelectedDate}
      />

      <CardReportModal
        open={activeModal === "card"}
        onClose={() => setActiveModal(null)}
        onGenerate={handleGenerateCardPdf}
        loading={loading}
      />
    </div>
  );
};

export default ReportPage;
