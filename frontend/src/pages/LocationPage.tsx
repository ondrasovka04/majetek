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

const LocationPage = () => {
  const printMutation = usePrintLocation();

  return (
    <DictionaryPage<Location>
      title="Umístění"
      addDialogTitle="Nové umístění"
      useItems={useLocations}
      useCreate={useCreateLocation}
      useUpdate={useUpdateLocation}
      useDelete={useDeleteLocation}
      extraRowActions={(loc, { notify, notifyError }) => (
        <IconButton
          onClick={() =>
            printMutation.mutate(loc.id, {
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
      )}
    />
  );
};

export default LocationPage;
