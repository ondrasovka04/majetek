/** Location dictionary page with print report action button. */
import { IconButton } from "@mui/material";
import PrintIcon from "@mui/icons-material/Print";
import DictionaryPage from "@/components/dictionary/DictionaryPage";
import { Location } from "@/types/location";
import {
  useLocations,
  useCreateLocation,
  useUpdateLocation,
  useDeleteLocation,
  usePrintLocation,
} from "@/hooks/queries/useLocationQueries";
import { translateError } from "@/utils/errorTranslations";
import { extractErrorMessage } from "@/utils/extractErrorMessage";

type RowHelpers = {
  notify: (msg: string, severity?: "success" | "error") => void;
  notifyError: (msg: string) => void;
};

const LocationPrintAction = ({ locId, notify, notifyError }: { locId: string } & RowHelpers) => {
  const printMutation = usePrintLocation();
  return (
    <IconButton
      onClick={() =>
        printMutation.mutate(locId, {
          onSuccess: (msg) => notify(msg, "success"),
          onError: (err: unknown) => {
            notifyError(translateError(extractErrorMessage(err)));
          },
        })
      }
      size="small"
      aria-label="Tisknout">
      <PrintIcon fontSize="small" />
    </IconButton>
  );
};

const locationExtraRowActions = (loc: Location, helpers: RowHelpers) => (
  <LocationPrintAction locId={loc.id} {...helpers} />
);

const LocationPage = () => (
  <DictionaryPage<Location>
    title="Umístění"
    addDialogTitle="Nové umístění"
    useItems={useLocations}
    useCreate={useCreateLocation}
    useUpdate={useUpdateLocation}
    useDelete={useDeleteLocation}
    extraRowActions={locationExtraRowActions}
  />
);

export default LocationPage;
