/**
 * Custom hook encapsulating inventory-difference state management.
 *
 * Responsibilities:
 * - Fetches raw differences from the server via TanStack Query.
 * - Derives default actions (KEEP / RL) for rows that haven't been assigned yet.
 * - Persists derived defaults back to the backend automatically.
 * - Provides `updateEntryAndSave` for optimistic local update + server patch.
 * - Exposes derived summary counts (writeOff, warn, wrong) and a validation flag.
 */
import { useEffect, useMemo, useRef, useState } from "react";
import { InventoryDifferenceEntry, InventoryDifferenceAction } from "@/types/inventoryAudit/inventoryDifference";
import type { PatchDifferenceBody } from "@/services/inventoryAuditService";
import { toIsoDateString } from "@/utils/formatDate";
import { translateError } from "@/utils/errorTranslations";
import { extractErrorMessage } from "@/utils/extractErrorMessage";
import {
  useInventoryDifferences,
  usePatchInventoryDifference,
  useProjectInventoryDifferences,
} from "@/hooks/queries/useInventoryAuditQueries";
import { useWriteOffMethods } from "@/hooks/queries/useWriteOffMethodQueries";

const getDerivedAction = (status: InventoryDifferenceEntry["status"]): InventoryDifferenceAction | null => {
  if (status === "CORRECT") return "KEEP";
  if (status === "WARN") return "RL";
  return null;
};

export function useDifferenceEntries(onError: (msg: string) => void) {
  const [localPatches, setLocalPatches] = useState<Map<number, Partial<InventoryDifferenceEntry>>>(new Map());

  const { data: rawDiffs, isLoading, error: diffsError } = useInventoryDifferences();
  const { data: writeOffMethods = [] } = useWriteOffMethods();
  const patchMutation = usePatchInventoryDifference();
  const projectMutation = useProjectInventoryDifferences();
  const initializedIdsRef = useRef<Set<number>>(new Set());
  const onErrorRef = useRef(onError);
  const patchRef = useRef(patchMutation.mutateAsync);
  useEffect(() => {
    onErrorRef.current = onError;
  }, [onError]);
  useEffect(() => {
    patchRef.current = patchMutation.mutateAsync;
  }, [patchMutation.mutateAsync]);

  // Derive noRecords from the error message instead of separate useState
  const noRecords = useMemo(() => {
    if (!diffsError) return false;
    return extractErrorMessage(diffsError) === "No inventory audit records found.";
  }, [diffsError]);

  // Notify non-"no records" errors
  useEffect(() => {
    if (diffsError && !noRecords) {
      onErrorRef.current(translateError(extractErrorMessage(diffsError)));
    }
  }, [diffsError, noRecords]);

  // Derive entries from query data + local patches (no setState in effect)
  const entries = useMemo(() => {
    if (!rawDiffs || writeOffMethods.length === 0) return [] as InventoryDifferenceEntry[];
    const defaultWriteOffMethodId = (writeOffMethods.find((m) => m.id === "A")?.id || writeOffMethods[0]?.id) ?? null;

    return rawDiffs.map((d) => {
      const patch = localPatches.get(d.cardId);
      const derivedAction = d.action ?? getDerivedAction(d.status);
      return {
        ...d,
        action: derivedAction,
        writeOffMethodId: d.writeOffMethodId ?? (derivedAction === "WRITE_OFF" ? defaultWriteOffMethodId : null),
        ...patch,
      };
    });
  }, [rawDiffs, writeOffMethods, localPatches]);

  // Persist derived defaults to backend (side effect only)
  useEffect(() => {
    if (!entries.length) return;
    const rawActionByCardId = new Map(rawDiffs?.map((d) => [d.cardId, d.action]) ?? []);
    const toInit = entries
      .filter((e) => e.action != null && !initializedIdsRef.current.has(e.cardId) && rawActionByCardId.get(e.cardId) == null)
      .map((e) => ({ cardId: e.cardId, action: e.action! }));
    if (toInit.length === 0) return;
    const ids = toInit.map((i) => i.cardId);
    ids.forEach((id) => initializedIdsRef.current.add(id));
    Promise.allSettled(
      toInit.map((i) =>
        patchRef
          .current({
            cardId: i.cardId,
            body: { action: i.action, writeOffDate: null, writeOffMethodId: null },
          })
          .catch((e: unknown) => {
            initializedIdsRef.current.delete(i.cardId);
            throw e;
          }),
      ),
    ).then((results) => {
      const failed = results.find((r) => r.status === "rejected") as PromiseRejectedResult | undefined;
      if (failed) onErrorRef.current(translateError(extractErrorMessage(failed.reason)));
    });
  }, [entries, rawDiffs]);

  /** Apply an optimistic local patch by cardId. */
  const applyPatch = (cardId: number, patch: Partial<InventoryDifferenceEntry>) => {
    setLocalPatches((prev) => new Map(prev).set(cardId, { ...prev.get(cardId), ...patch }));
  };

  /** Remove local patch (rollback). */
  const removePatch = (cardId: number) => {
    setLocalPatches((prev) => {
      const next = new Map(prev);
      next.delete(cardId);
      return next;
    });
  };

  /** Optimistically update a single entry locally and persist the change. */
  const updateEntryAndSave = (cardId: number, patch: Partial<InventoryDifferenceEntry>) => {
    const prevEntry = entries.find((e) => e.cardId === cardId);
    if (!prevEntry) return;

    const nextEntry: InventoryDifferenceEntry = {
      ...prevEntry,
      ...patch,
    } as InventoryDifferenceEntry;

    if (nextEntry.action === "WRITE_OFF" && !nextEntry.writeOffMethodId) {
      nextEntry.writeOffMethodId = (writeOffMethods.find((m) => m.id === "A")?.id || writeOffMethods[0]?.id) ?? null;
    }

    applyPatch(cardId, { ...patch, writeOffMethodId: nextEntry.writeOffMethodId });

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
    } else if (nextEntry.action === "WRITE_OFF" && (dateChanged || methodChanged)) {
      payload.action = "WRITE_OFF";
      payload.writeOffDate = toIsoDateString(nextEntry.writeOffDate);
      payload.writeOffMethodId = nextEntry.writeOffMethodId ?? null;
    }

    if (Object.keys(payload).length === 0) return;

    patchMutation.mutate(
      { cardId, body: payload },
      {
        onError: (err: unknown) => {
          removePatch(cardId);
          onErrorRef.current(translateError(extractErrorMessage(err)));
        },
      },
    );
  };

  // --- Derived summary values ---
  const hasInvalid = useMemo(() => {
    return entries.some((e) => {
      if (e.status === "WRONG" && !e.action) return true;
      if (e.action === "WRITE_OFF" && (!e.writeOffDate || !e.writeOffMethodId)) return true;
      return false;
    });
  }, [entries]);

  const writeOffCount = useMemo(() => entries.filter((e) => e.action === "WRITE_OFF").length, [entries]);
  const warnCount = useMemo(() => entries.filter((e) => e.status === "WARN").length, [entries]);
  const wrongCount = useMemo(() => entries.filter((e) => e.status === "WRONG").length, [entries]);

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
