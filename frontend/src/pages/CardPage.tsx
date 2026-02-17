/**
 * Main card management page (master-detail layout).
 * Left panel: searchable/sortable card list (CardListPanel).
 * Right panel: card detail (edit) or create form.
 */
import { useMemo, useRef, useState } from "react";
import { Typography, Paper } from "@mui/material";
import { useQueryClient } from "@tanstack/react-query";
import MenuBar from "../components/MenuBar";
import CardDetailView from "../components/card/CardDetailView";
import CardCreateView from "../components/card/CardCreateView";
import CardListPanel, { CardListPanelHandle } from "../components/card/CardListPanel";
import { CardFormValues } from "../schemas/cardSchema";
import { useNotification } from "../hooks/useNotification";
import { useUsers } from "../hooks/queries/useCardQueries";
import { useLocations } from "../hooks/queries/useLocationQueries";
import { useClasses } from "../hooks/queries/useClassQueries";
import { useAssetGroups } from "../hooks/queries/useAssetGroupQueries";
import { usePurchaseMethods } from "../hooks/queries/usePurchaseMethodQueries";
import { useWriteOffMethods } from "../hooks/queries/useWriteOffMethodQueries";
import { sortById } from "../utils/czech";

const CardPage = () => {
  const [selectedCardId, setSelectedCardId] = useState<string | null>(null);
  const [duplicatedCard, setDuplicatedCard] = useState<Partial<CardFormValues> | null>(null);
  const { notify, notifyError, NotificationBar } = useNotification();
  const queryClient = useQueryClient();
  const listRef = useRef<CardListPanelHandle>(null);

  // --- Dictionary data (sorted for detail/create forms) ---
  const { data: rawLocations = [] } = useLocations();
  const { data: rawClasses = [] } = useClasses();
  const { data: rawGroups = [] } = useAssetGroups();
  const { data: rawPurchaseMethods = [] } = usePurchaseMethods();
  const { data: rawWriteOffMethods = [] } = useWriteOffMethods();
  const { data: rawUsers = [] } = useUsers();

  const locations = useMemo(() => sortById(rawLocations), [rawLocations]);
  const classes = useMemo(() => sortById(rawClasses), [rawClasses]);
  const groups = useMemo(() => sortById(rawGroups), [rawGroups]);
  const purchaseMethods = useMemo(() => sortById(rawPurchaseMethods), [rawPurchaseMethods]);
  const writeOffMethods = useMemo(() => sortById(rawWriteOffMethods), [rawWriteOffMethods]);
  const users = useMemo(() => sortById(rawUsers), [rawUsers]);

  return (
    <div className="min-h-screen bg-gray-50">
      <MenuBar />
      {NotificationBar}
      <div className="p-6 flex">
        <CardListPanel
          ref={listRef}
          selectedCardId={selectedCardId}
          onSelectCard={setSelectedCardId}
          onNewCard={() => setSelectedCardId("NEW")}
          onNotify={notify}
          onError={notifyError}
        />

        <Paper className="w-2/3 p-4" elevation={3}>
          {selectedCardId === "NEW" ? (
            <CardCreateView
              prefillCard={duplicatedCard ?? undefined}
              onCreated={() => {
                setDuplicatedCard(null);
                setSelectedCardId(null);
                listRef.current?.refilter();
              }}
              onError={notifyError}
              onSuccess={(msg: string) => notify(msg, "success")}
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
              onChange={() => {
                queryClient.invalidateQueries({ queryKey: ["cards"] });
                notify("Změny byly úspěšně uloženy.", "success");
              }}
              onDeleted={() => {
                setSelectedCardId(null);
                notify("Karta byla úspěšně smazána.", "success");
              }}
              onDuplicate={(dup) => {
                setDuplicatedCard(dup);
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
    </div>
  );
};

export default CardPage;
