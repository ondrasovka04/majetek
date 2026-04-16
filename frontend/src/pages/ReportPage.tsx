/** PDF report generation page. Offers group, location, and individual card reports with an embedded PDF viewer. */
import { useState } from "react";
import { Paper, Typography, List, ListItemButton, CircularProgress } from "@mui/material";
import MenuBar from "@/components/MenuBar";
import { translateError } from "@/utils/errorTranslations";
import { extractErrorMessage } from "@/utils/extractErrorMessage";
import { Worker, Viewer } from "@react-pdf-viewer/core";
import "@react-pdf-viewer/core/lib/styles/index.css";
import "@react-pdf-viewer/default-layout/lib/styles/index.css";
import { defaultLayoutPlugin } from "@react-pdf-viewer/default-layout";
import GroupReportModal, { ALL_GROUPS } from "@/components/report/GroupReportModal";
import czechLocalization from "@/utils/czechLocalization";
import LocationReportModal from "@/components/report/LocationReportModal";
import CardReportModal from "@/components/report/CardReportModal";
import { useNotification } from "@/hooks/useNotification";
import { useAssetGroups } from "@/hooks/queries/useAssetGroupQueries";
import { useLocations } from "@/hooks/queries/useLocationQueries";
import { useExportCardPdf, useExportGroupPdf, useExportLocationPdf } from "@/hooks/queries/useCardQueries";
import { sortById } from "@/utils/czech";

type ActiveModal = "group" | "location" | "card" | undefined;

const ReportPage = () => {
  const [selectedGroup, setSelectedGroup] = useState("");
  const [selectedLocation, setSelectedLocation] = useState("");
  const [pdfUrl, setPdfUrl] = useState<string | undefined>();
  const [activeModal, setActiveModal] = useState<ActiveModal>();
  const [selectedDate, setSelectedDate] = useState<Date | null>(new Date());

  const { notifyError, NotificationBar } = useNotification();

  // --- TanStack Query ---
  const { data: rawGroups = [] } = useAssetGroups();
  const { data: rawLocations = [] } = useLocations();
  const groups = sortById(rawGroups);
  const locations = sortById(rawLocations);

  const groupPdfMutation = useExportGroupPdf();
  const locationPdfMutation = useExportLocationPdf();
  const cardPdfMutation = useExportCardPdf();

  const loading = groupPdfMutation.isPending || locationPdfMutation.isPending || cardPdfMutation.isPending;

  const defaultLayoutPluginInstance = defaultLayoutPlugin({
    renderToolbar: (Toolbar) => (
      <Toolbar>
        {(slots) => (
          <div
            style={{
              alignItems: "center",
              display: "flex",
              width: "100%",
            }}>
            <div
              style={{
                alignItems: "center",
                display: "flex",
              }}>
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
              }}>
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
              }}>
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

  const resetModalState = () => {
    setSelectedGroup("");
    setSelectedLocation("");
    setSelectedDate(new Date());
  };

  const handleCloseModal = () => {
    setActiveModal(undefined);
    resetModalState();
  };

  const handleGenerateGroupPdf = () => {
    if (!selectedGroup) {
      notifyError("Musíte vybrat skupinu.");
      return;
    }
    setActiveModal(undefined);
    const groupId = selectedGroup === ALL_GROUPS ? "" : selectedGroup;
    groupPdfMutation.mutate(
      { groupId, selectedDate },
      {
        onSuccess: (objectUrl) => {
          setPdfUrl(objectUrl);
          resetModalState();
        },
        onError: (err: unknown) => {
          notifyError(translateError(extractErrorMessage(err)));
        },
      },
    );
  };

  const handleGenerateLocationPdf = () => {
    if (!selectedLocation) {
      notifyError("Musíte vybrat místnost.");
      return;
    }
    setActiveModal(undefined);
    locationPdfMutation.mutate(
      { locationId: selectedLocation, selectedDate },
      {
        onSuccess: (objectUrl) => {
          setPdfUrl(objectUrl);
          resetModalState();
        },
        onError: (err: unknown) => {
          notifyError(translateError(extractErrorMessage(err)));
        },
      },
    );
  };

  const handleGenerateCardPdf = (cardId: number) => {
    setActiveModal(undefined);
    cardPdfMutation.mutate(cardId, {
      onSuccess: (result) => {
        setPdfUrl(result.url);
      },
      onError: (err: unknown) => {
        notifyError(translateError(extractErrorMessage(err)));
      },
    });
  };

  const renderPdfPanel = () => {
    if (loading) {
      return (
        <div className="flex justify-center items-center h-full">
          <CircularProgress />
        </div>
      );
    }
    if (pdfUrl) {
      return (
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
      );
    }
    return (
      <Typography variant="body1" color="textSecondary">
        Vyberte report vlevo pro zobrazení.
      </Typography>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <MenuBar />
      <div className="p-6 flex">
        {NotificationBar}

        <Paper className="w-1/3 mr-4 p-4" elevation={3}>
          <Typography variant="h6" gutterBottom>
            Sestavy
          </Typography>
          <List>
            <ListItemButton onClick={() => setActiveModal("group")}>Podle skupin</ListItemButton>
            <ListItemButton onClick={() => setActiveModal("location")}>Podle místností</ListItemButton>
            <ListItemButton onClick={() => setActiveModal("card")}>Karta majetku</ListItemButton>
          </List>
        </Paper>

        <Paper
          className="p-4"
          elevation={3}
          style={{
            width: "66.6667%",
            height: pdfUrl ? "calc(100vh - 120px)" : "auto",
          }}>
          {renderPdfPanel()}
        </Paper>
      </div>

      <GroupReportModal
        open={activeModal === "group"}
        onClose={handleCloseModal}
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
        onClose={handleCloseModal}
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
        onClose={handleCloseModal}
        onGenerate={handleGenerateCardPdf}
        loading={loading}
      />
    </div>
  );
};

export default ReportPage;
