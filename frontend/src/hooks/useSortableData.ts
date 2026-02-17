/** Generic hook for sorting tabular data. Uses Czech locale collation and pushes empty names to the end. */
import { useState, useMemo, useCallback } from "react";
import { czechCollator } from "../utils/czech";

type SortDirection = "asc" | "desc";

interface SortConfig<K extends string> {
  key: K;
  direction: SortDirection;
}

export function useSortableData<T>(
  data: T[],
  defaultKey: string & keyof T,
  defaultDirection: SortDirection = "asc",
  nameKeys: (string & keyof T)[] = ["name" as string & keyof T],
) {
  const [sortConfig, setSortConfig] = useState<SortConfig<string & keyof T>>({
    key: defaultKey,
    direction: defaultDirection,
  });

  const handleSort = useCallback((key: string & keyof T) => {
    setSortConfig((prev) =>
      prev.key === key
        ? { key, direction: prev.direction === "asc" ? "desc" : "asc" }
        : { key, direction: "asc" },
    );
  }, []);

  const sortedData = useMemo(() => {
    const { key, direction } = sortConfig;
    return [...data].sort((a, b) => {
      const aVal = String((a as Record<string, unknown>)[key] ?? "");
      const bVal = String((b as Record<string, unknown>)[key] ?? "");

      if (nameKeys.includes(key)) {
        if (!aVal.trim()) return 1;
        if (!bVal.trim()) return -1;
        return direction === "asc"
          ? czechCollator.compare(aVal, bVal)
          : czechCollator.compare(bVal, aVal);
      }

      return direction === "asc"
        ? aVal.localeCompare(bVal, "cs", { numeric: true })
        : bVal.localeCompare(aVal, "cs", { numeric: true });
    });
  }, [data, sortConfig, nameKeys]);

  return { sortedData, sortConfig, handleSort };
}
