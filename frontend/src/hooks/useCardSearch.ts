/** Fuse.js-powered search, write-off filtering, and Czech-locale sorting for the card list. */
import { useMemo } from "react";
import Fuse from "fuse.js";
import { Card } from "@/types/card/card";
import { normalizeString, czechCollatorBase } from "@/utils/czech";

interface SortConfig {
  key: keyof Card;
  direction: "asc" | "desc";
}

export function useCardSearch(cards: Card[], search: string, showWrittenOff: boolean, sortConfig: SortConfig) {
  const searchableCards = useMemo(
    () =>
      cards.map((card) => ({
        ...card,
        normalizedName: normalizeString(card.name),
      })),
    [cards],
  );

  const fuse = useMemo(
    () =>
      new Fuse(searchableCards, {
        keys: ["id", "normalizedName"],
        includeScore: false,
        minMatchCharLength: 1,
        ignoreLocation: true,
        threshold: 0,
      }),
    [searchableCards],
  );

  const filteredCards = useMemo(() => {
    let result = cards;
    if (!showWrittenOff) result = result.filter((c) => !c.writeOffDate);
    if (!search) return result;
    const matchingIds = new Set(fuse.search(normalizeString(search)).map((r) => r.item.id));
    return result.filter((c) => matchingIds.has(c.id));
  }, [search, cards, fuse, showWrittenOff]);

  const sortedCards = useMemo(() => {
    return [...filteredCards].sort((a, b) => {
      const { key, direction } = sortConfig;
      const aVal = a[key] ?? "";
      const bVal = b[key] ?? "";

      if (key === "name") {
        const cmp = czechCollatorBase.compare(String(aVal), String(bVal));
        if (cmp !== 0) return direction === "asc" ? cmp : -cmp;
        return Number(a.id) - Number(b.id);
      }

      return direction === "asc"
        ? String(aVal).localeCompare(String(bVal), "cs", { numeric: true })
        : String(bVal).localeCompare(String(aVal), "cs", { numeric: true });
    });
  }, [filteredCards, sortConfig]);

  return sortedCards;
}
