/**
 * Shared Czech-locale helpers: collators, diacritics stripping, and ID-based sorting.
 */

// --- Collators ---

/** Czech collator ignoring accents and case — ideal for search matching. */
export const czechCollatorBase = new Intl.Collator("cs", {
  sensitivity: "base",
  usage: "sort",
});

/** Czech collator respecting accents and case order — ideal for display sort. */
export const czechCollator = new Intl.Collator("cs", {
  sensitivity: "variant",
  usage: "sort",
  caseFirst: "false",
});

// --- String helpers ---

/** Strips diacritics for accent-insensitive matching (used by Fuse.js). */
export const normalizeString = (str: string) =>
  str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");

/** Sorts items by their `id` property using the given collator (defaults to {@link czechCollator}). */
export const sortById = <T extends { id: string }>(
  items: T[],
  collator: Intl.Collator = czechCollator,
) => [...items].sort((a, b) => collator.compare(a.id, b.id));
