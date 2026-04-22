/**
 * Card detail panel (right side of the master-detail layout).
 * Loads full card data via useCardDetail, provides save/delete/duplicate/PDF actions,
 * and manages the attachment modal.
 */
import { Typography, CircularProgress, Tooltip, IconButton } from "@mui/material";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import AttachmentModal from "@/components/card/AttachmentModal";
import SaveIcon from "@mui/icons-material/Save";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { useState, useEffect, useRef } from "react";
import { useForm, FormProvider } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { translateError } from "@/utils/errorTranslations";
import { Class } from "@/types/class";
import { AssetGroup } from "@/types/assetGroup";
import { Location } from "@/types/location";
import { PurchaseMethod } from "@/types/purchaseMethod";
import { WriteOffMethod } from "@/types/writeOffMethod";
import { User } from "@/types/auth";
import DeleteIcon from "@mui/icons-material/Delete";
import DownloadIcon from "@mui/icons-material/Download";
import ConfirmDialog from "@/components/card/ConfirmDialog";
import { saveAs } from "file-saver";
import { cardSchema, CardFormValues, CardFormOutput } from "@/schemas/cardSchema";
import CardFormFields from "@/components/card/CardFormFields";
import { useNotification } from "@/hooks/useNotification";
import { extractErrorMessage } from "@/utils/extractErrorMessage";
import { useCardDetail, useUpdateCard, useDeleteCard, useExportCardPdf } from "@/hooks/queries/useCardQueries";
import { useAttachments } from "@/hooks/queries/useAttachmentQueries";

interface Props {
  cardId: string;
  onChange?: () => void;
  onDeleted?: () => void;
  onDuplicate?: (duplicatedCard: Partial<CardFormValues>) => void;
  locations: Location[];
  groups: AssetGroup[];
  classes: Class[];
  purchaseMethods: PurchaseMethod[];
  writeOffMethods: WriteOffMethod[];
  users: User[];
}

const CardDetailView = ({
  cardId,
  onChange,
  onDeleted,
  onDuplicate,
  locations,
  groups,
  classes,
  purchaseMethods,
  writeOffMethods,
  users,
}: Props) => {
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [attachmentModalOpen, setAttachmentModalOpen] = useState(false);
  const { notify, notifyError, NotificationBar } = useNotification();

  // --- TanStack Query ---
  const { data: cardDetail, isLoading } = useCardDetail(cardId);
  const { data: attachments = [] } = useAttachments(cardId);
  const updateMutation = useUpdateCard();
  const deleteMutation = useDeleteCard();
  const exportPdfMutation = useExportCardPdf();

  const methods = useForm<CardFormValues, unknown, CardFormOutput>({
    resolver: zodResolver(cardSchema),
    defaultValues: {},
    mode: "onSubmit",
  });

  // Track originalName + originalWriteOffDate to detect list-relevant changes
  const originalNameRef = useRef<string>("");
  const originalWriteOffDateRef = useRef<Date | null>(null);

  // Reset form when query data arrives or cardId changes
  useEffect(() => {
    if (!cardDetail) return;
    originalNameRef.current = cardDetail.name;
    originalWriteOffDateRef.current = cardDetail.writeOffDate;

    methods.reset({
      name: cardDetail.name,
      price: cardDetail.price ?? null,
      quantity: cardDetail.quantity ?? null,
      serialNumber: cardDetail.serialNumber ?? "",
      locationId: cardDetail.locationId,
      classId: cardDetail.classId ?? "",
      groupId: cardDetail.groupId,
      purchaseMethodId: cardDetail.purchaseMethodId ?? "",
      purchaseDate: cardDetail.purchaseDate,
      purchaseInvoice: cardDetail.purchaseInvoice ?? "",
      writeOffDate: cardDetail.writeOffDate,
      writeOffMethodId: cardDetail.writeOffMethodId ?? "",
      responsiblePersonId: cardDetail.responsiblePersonId ?? "",
      note: cardDetail.note ?? "",
      id: cardDetail.id,
      createdById: cardDetail.createdById,
      createdDate: cardDetail.createdDate,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cardDetail]);

  const onSubmit = (data: CardFormOutput) => {
    updateMutation.mutate(
      { id: cardId, card: data },
      {
        onSuccess: () => {
          originalNameRef.current = data.name;
          originalWriteOffDateRef.current = data.writeOffDate;

          if (
            data.name !== originalNameRef.current ||
            data.writeOffDate?.getTime() !== originalWriteOffDateRef.current?.getTime()
          ) {
            onChange?.();
          }
          notify("Změny byly úspěšně uloženy.", "success");
        },
        onError: (e: unknown) => {
          notifyError(translateError(extractErrorMessage(e)));
        },
      },
    );
  };

  const onInvalid = () => {
    const firstError = Object.values(methods.formState.errors)[0];
    if (firstError?.message) {
      notifyError(String(firstError.message));
    }
  };

  const handleDelete = () => {
    deleteMutation.mutate(cardId, {
      onSuccess: () => onDeleted?.(),
      onError: (e: unknown) => {
        notifyError(translateError(extractErrorMessage(e)));
      },
      onSettled: () => setConfirmOpen(false),
    });
  };

  const handleReportDownload = () => {
    exportPdfMutation.mutate(Number(cardId), {
      onSuccess: (result) => {
        saveAs(result.blob, `karta-${cardId}.pdf`);
        notify("Sestava byla úspěšně stažena.", "success");
      },
      onError: (e: unknown) => {
        notifyError(translateError(extractErrorMessage(e)));
      },
    });
  };

  const handleDuplicate = () => {
    const values = methods.getValues();
    onDuplicate?.({
      ...values,
      id: undefined,
      createdDate: undefined,
      createdById: undefined,
    });
  };

  if (isLoading) {
    return <CircularProgress />;
  }

  return (
    <div>
      {NotificationBar}

      <div className="flex items-center justify-between mb-2">
        <Typography variant="h6">Karta #{cardId}</Typography>
        <div className="flex items-center justify-between mb-2">
          <Tooltip title="Uložit změny">
            <span>
              <IconButton
                onClick={() => methods.handleSubmit(onSubmit, onInvalid)()}
                color="primary"
                disabled={updateMutation.isPending}>
                {updateMutation.isPending ? <CircularProgress size={24} /> : <SaveIcon />}
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Duplikovat">
            <span>
              <IconButton onClick={handleDuplicate} color="primary">
                <ContentCopyIcon />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Spravovat přílohy">
            <span>
              <IconButton
                onClick={() => setAttachmentModalOpen(true)}
                color={attachments.length > 0 ? "primary" : "default"}>
                <AttachFileIcon />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Stáhnout sestavu">
            <span>
              <IconButton onClick={handleReportDownload} color="primary">
                <DownloadIcon />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Smazat kartu">
            <span>
              <IconButton onClick={() => setConfirmOpen(true)} color="error">
                <DeleteIcon />
              </IconButton>
            </span>
          </Tooltip>
        </div>
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
            showReadOnlyFields
          />
        </FormProvider>
      </div>

      <AttachmentModal
        open={attachmentModalOpen}
        onClose={() => setAttachmentModalOpen(false)}
        cardId={cardId}
        onError={notifyError}
      />
      <ConfirmDialog
        open={confirmOpen}
        title="Smazat kartu?"
        description="Opravdu chcete smazat tuto kartu? Tuto akci nelze vrátit zpět."
        onCancel={() => setConfirmOpen(false)}
        onConfirm={handleDelete}
      />
    </div>
  );
};

export default CardDetailView;
