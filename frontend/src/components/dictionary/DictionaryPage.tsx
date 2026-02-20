import { useState } from "react";
import {
  Typography,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  CircularProgress,
  ThemeProvider,
  CssBaseline,
} from "@mui/material";
import { UseQueryResult, UseMutationResult } from "@tanstack/react-query";
import MenuBar from "../MenuBar";
import { DictionaryEntity } from "../../types/dictionary";
import DictionaryRow from "./DictionaryRow";
import AddDictionaryDialog from "./AddDictionaryDialog";
import { useNotification } from "../../hooks/useNotification";
import { useSortableData } from "../../hooks/useSortableData";
import { translateError } from "../../utils/errorTranslations";
import { extractErrorMessage } from "../../utils/extractErrorMessage";
import { dictionaryTheme } from "../../theme";

interface DictionaryPageProps<T extends DictionaryEntity> {
  readonly title: string;
  readonly addDialogTitle: string;
  /** Maximum length of the ID field in the add dialog (default: 20) */
  readonly maxIdLength?: number;
  readonly useItems: () => UseQueryResult<T[]>;
  readonly useCreate: () => UseMutationResult<T, unknown, T, unknown>;
  readonly useUpdate: () => UseMutationResult<unknown, unknown, { id: string; name: string | null }, unknown>;
  readonly useDelete: () => UseMutationResult<unknown, unknown, string, unknown>;
  /** Optional render function for extra actions per row (e.g. print button on Location).
   *  Receives notify helpers from the page's notification system. */
  readonly extraRowActions?: (
    item: T,
    helpers: { notify: (msg: string, severity?: "success" | "error") => void; notifyError: (msg: string) => void },
  ) => React.ReactNode;
}

export default function DictionaryPage<T extends DictionaryEntity>({
  title,
  addDialogTitle,
  maxIdLength,
  useItems,
  useCreate,
  useUpdate,
  useDelete,
  extraRowActions,
}: DictionaryPageProps<T>) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const { notify, notifyError, NotificationBar } = useNotification();

  const { data: items = [], isLoading } = useItems();
  const createMutation = useCreate();
  const updateMutation = useUpdate();
  const deleteMutation = useDelete();

  const { sortedData, sortConfig, handleSort } = useSortableData(items, "id" as string & keyof T);

  const handleCreate = (item: T): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      createMutation.mutate(item, {
        onSuccess: () => resolve(),
        onError: (err: unknown) => {
          notifyError(translateError(extractErrorMessage(err)));
          reject(err);
        },
      });
    });
  };

  const handleUpdate = (vars: { id: string; name: string | null }) => {
    updateMutation.mutate(vars, {
      onError: (err: unknown) => {
        notifyError(translateError(extractErrorMessage(err)));
      },
    });
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id, {
      onError: (err: unknown) => {
        notifyError(translateError(extractErrorMessage(err)));
      },
    });
  };

  return (
    <ThemeProvider theme={dictionaryTheme}>
      <CssBaseline />
      <div>
        <MenuBar />
        {NotificationBar}
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <Typography variant="h4">{title}</Typography>
            <Button variant="contained" onClick={() => setDialogOpen(true)}>
              Přidat
            </Button>
          </div>

          {isLoading ? (
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
                        onClick={() => handleSort("id" as string & keyof T)}
                        style={{ cursor: "pointer", fontWeight: "bold" }}>
                        ID {sortConfig.key === "id" && (sortConfig.direction === "asc" ? "▲" : "▼")}
                      </TableCell>
                      <TableCell
                        onClick={() => handleSort("name" as string & keyof T)}
                        style={{ cursor: "pointer", fontWeight: "bold" }}>
                        Název {sortConfig.key === "name" && (sortConfig.direction === "asc" ? "▲" : "▼")}
                      </TableCell>
                      <TableCell style={{ fontWeight: "bold" }}>Akce</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {sortedData.map((item) => (
                      <DictionaryRow
                        key={item.id}
                        item={item}
                        onUpdate={handleUpdate}
                        onDelete={handleDelete}
                        onError={notifyError}
                        extraActions={
                          extraRowActions ? (i: T) => extraRowActions(i, { notify, notifyError }) : undefined
                        }
                      />
                    ))}
                  </TableBody>
                </Table>
              </div>

              <AddDictionaryDialog<T>
                open={dialogOpen}
                onClose={() => setDialogOpen(false)}
                onCreate={handleCreate}
                title={addDialogTitle}
                maxIdLength={maxIdLength}
              />
            </>
          )}
        </div>
      </div>
    </ThemeProvider>
  );
}
