/** Form for creating a new asset card. Pre-fills from optional duplicated values. */
import { Typography, IconButton, CircularProgress, Tooltip } from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import { useForm, FormProvider } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { translateError } from "../../utils/errorTranslations";
import { Class } from "../../types/class";
import { AssetGroup } from "../../types/assetGroup";
import { Location } from "../../types/location";
import { PurchaseMethod } from "../../types/purchaseMethod";
import { WriteOffMethod } from "../../types/writeOffMethod";
import { User } from "../../types/auth";
import { cardSchema, CardFormValues, CardFormOutput, defaultCardValues } from "../../schemas/cardSchema";
import CardFormFields from "./CardFormFields";
import { extractErrorMessage } from "../../utils/extractErrorMessage";
import { useCreateCard } from "../../hooks/queries/useCardQueries";

interface Props {
  onCreated: () => void;
  onError: (msg: string) => void;
  onSuccess: (msg: string) => void;
  prefillCard?: Partial<CardFormValues>;
  locations: Location[];
  groups: AssetGroup[];
  classes: Class[];
  purchaseMethods: PurchaseMethod[];
  writeOffMethods: WriteOffMethod[];
  users: User[];
}

const CardCreateView = ({
  onCreated,
  onError,
  onSuccess,
  prefillCard,
  locations,
  groups,
  classes,
  purchaseMethods,
  writeOffMethods,
  users,
}: Props) => {
  const createMutation = useCreateCard();

  const methods = useForm<CardFormValues, unknown, CardFormOutput>({
    resolver: zodResolver(cardSchema),
    defaultValues: { ...defaultCardValues, ...prefillCard },
    mode: "onSubmit",
  });

  const onSubmit = (data: CardFormOutput) => {
    createMutation.mutate(data, {
      onSuccess: () => {
        onSuccess("Karta úspěšně vytvořena.");
        onCreated();
      },
      onError: (e: unknown) => {
        onError(translateError(extractErrorMessage(e)));
      },
    });
  };

  const onInvalid = () => {
    const firstError = Object.values(methods.formState.errors)[0];
    if (firstError?.message) {
      onError(String(firstError.message));
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <Typography variant="h6">Nová karta</Typography>
        <Tooltip title="Uložit">
          <span>
            <IconButton
              onClick={methods.handleSubmit(onSubmit, onInvalid)}
              color="primary"
              disabled={createMutation.isPending}>
              {createMutation.isPending ? <CircularProgress size={24} /> : <SaveIcon />}
            </IconButton>
          </span>
        </Tooltip>
      </div>

      <div
        style={{
          maxHeight: "calc(100vh - 180px)",
          overflowY: "auto",
          paddingRight: 8,
          paddingTop: 8,
        }}>
        <FormProvider {...methods}>
          <CardFormFields
            locations={locations}
            groups={groups}
            classes={classes}
            purchaseMethods={purchaseMethods}
            writeOffMethods={writeOffMethods}
            users={users}
          />
        </FormProvider>
      </div>
    </div>
  );
};

export default CardCreateView;
