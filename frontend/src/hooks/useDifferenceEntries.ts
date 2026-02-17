/**
 * Custom hook encapsulating inventory-difference state management.
 *
 * Responsibilities:
 * - Fetches raw differences from the server via TanStack Query.
 * - Derives default actions (KEEP / SU) for rows that haven't been assigned yet.
 * - Persists derived defaults back to the backend automatically.
 * - Provides `updateEntryAndSave` for optimistic local update + server patch.
 * - Exposes derived summary counts (writeOff, warn, wrong) and a validation flag.
 */
import { useEffect, useMemo, useState } from "react";
import {
  InventoryDifferenceEntry,
  InventoryDifferenceAction,
} from "../types/inventoryAudit/inventoryDifference";
import type { PatchDifferenceBody } from "../services/inventoryAuditService";
import { toIsoDateString } from "../utils/formatDate";
import { translateError } from "../utils/errorTranslations";
import { extractErrorMessage } from "../utils/extractErrorMessage";
import {
  useInventoryDifferences,
  usePatchInventoryDifference,
  useProjectInventoryDifferences,
} from "./queries/useInventoryAuditQueries";
import { useWriteOffMethods } from "./queries/useWriteOffMethodQueries";

export function useDifferenceEntries(onError: (msg: string) => void) {
  const [entries, setEntries] = useState<InventoryDifferenceEntry[]>([]);

  const {
    data: rawDiffs,
    isLoading,
    error: diffsError,
  } = useInventoryDifferences();
  const { data: writeOffMethods = [] } = useWriteOffMethods();
  const patchMutation = usePatchInventoryDifference();
  const projectMutation = useProjectInventoryDifferences();

  // Derive noRecords from the error message instead of separate useState
  const noRecords = useMemo(() => {
    if (!diffsError) return false;
    return (
      extractErrorMessage(diffsError) === "No inventory audit records found."
    );
  }, [diffsError]);

  // Notify non-"no records" errors
  useEffect(() => {
    if (diffsError && !noRecords) {
      onError(translateError(extractErrorMessage(diffsError)));
    }
  }, [diffsError, noRecords]);

  // Initialize local entries from query data + derive default actions
  useEffect(() => {
    if (!rawDiffs || writeOffMethods.length === 0) return;
    const defaultWriteOffMethodId =
      (writeOffMethods.find((m) => m.id === "A")?.id ||
        writeOffMethods[0]?.id) ??
      null;

    const toInit: { cardId: number; action: InventoryDifferenceAction }[] = [];

    const mapped = rawDiffs.map((d) => {
      const derivedAction =
        d.action ??
        (d.status === "CORRECT"
          ? "KEEP"
          : d.status === "WARN"
            ? "SU"
            : null);
      if (d.action == null && derivedAction) {
        toInit.push({ cardId: d.cardId, action: derivedAction });
      }
      return {
        ...d,
        action: derivedAction,
        writeOffMethodId:
          d.writeOffMethodId ??
          (derivedAction === "WRITE_OFF" ? defaultWriteOffMethodId : null),
      };
    });
    setEntries(mapped);

    // Persist derived defaults to backend
    if (toInit.length > 0) {
      Promise.all(
        toInit.map((i) =>
          patchMutation.mutateAsync({
            cardId: i.cardId,
            body: {
              action: i.action,
              writeOffDate: null,
              writeOffMethodId: null,
            },
          }),
        ),
      ).catch((e: unknown) => {
        onError(translateError(extractErrorMessage(e)));
      });
    }
  }, [rawDiffs, writeOffMethods]);

  /** Optimistically update a single entry locally and persist the change. */
  const updateEntryAndSave = (
    cardId: number,
    patch: Partial<InventoryDifferenceEntry>,
  ) => {
    const prevEntry = entries.find((e) => e.cardId === cardId);
    if (!prevEntry) return;

    const nextEntry: InventoryDifferenceEntry = {
      ...prevEntry,
      ...patch,
    } as InventoryDifferenceEntry;

    if (nextEntry.action === "WRITE_OFF" && !nextEntry.writeOffMethodId) {
      nextEntry.writeOffMethodId =
        (writeOffMethods.find((m) => m.id === "A")?.id ||
          writeOffMethods[0]?.id) ??
        null;
    }

    setEntries((prev) =>
      prev.map((e) => (e.cardId === cardId ? nextEntry : e)),
    );

    // Build PATCH payload
    const payload: PatchDifferenceBody = {};
    const actionChanged = patch.action !== undefined;
    const dateChanged = patch.writeOffDate !== undefined;
    const methodChanged = patch.writeOffMethodId !== undefined;

    // 1) If action changed → always send action; clear write-off fields when not WRITE_OFF.
    // 2) If action is WRITE_OFF and date/method changed → send all three.
    // 3) If no action change but current is WRITE_OFF and date/method changed → send action + values.
    if (actionChanged) {
      payload.action = nextEntry.action ?? null;
      if (nextEntry.action === "WRITE_OFF") {
        payload.writeOffDate = toIsoDateString(nextEntry.writeOffDate);
        payload.writeOffMethodId = nextEntry.writeOffMethodId ?? null;
      } else {
        payload.writeOffDate = null;
        payload.writeOffMethodId = null;
      }
    } else if (
      nextEntry.action === "WRITE_OFF" &&
      (dateChanged || methodChanged)
    ) {
      payload.action = "WRITE_OFF";
      payload.writeOffDate = toIsoDateString(nextEntry.writeOffDate);
      payload.writeOffMethodId = nextEntry.writeOffMethodId ?? null;
    }

    if (Object.keys(payload).length === 0) return;

    patchMutation.mutate(
      { cardId, body: payload },
      {
        onError: (err: unknown) => {
          // Rollback on failure
          setEntries((prev) =>
            prev.map((e) => (e.cardId === cardId ? prevEntry : e)),
          );
          onError(translateError(extractErrorMessage(err)));
        },
      },
    );
  };

  // --- Derived summary values ---
  const hasInvalid = useMemo(() => {
    return entries.some((e) => {
      if (e.status === "WRONG" && !e.action) return true;
      if (e.action === "WRITE_OFF" && (!e.writeOffDate || !e.writeOffMethodId))
        return true;
      return false;
    });
  }, [entries]);

  const writeOffCount = useMemo(
    () => entries.filter((e) => e.action === "WRITE_OFF").length,
    [entries],
  );
  const warnCount = useMemo(
    () => entries.filter((e) => e.status === "WARN").length,
    [entries],
  );
  const wrongCount = useMemo(
    () => entries.filter((e) => e.status === "WRONG").length,
    [entries],
  );

  return {
    entries,
    loading: isLoading,
    noRecords,
    writeOffMethods,
    updateEntryAndSave,
    hasInvalid,
    writeOffCount,
    warnCount,
    wrongCount,
    projectMutation,
  };
}
