import { describe, it, expect } from "vitest";
import { renderHook } from "@testing-library/react";
import { useCardSearch } from "@/hooks/useCardSearch";
import { Card } from "@/types/card/card";

const makeCard = (id: string, name: string, writeOffDate: Date | null = null): Card => ({
  id,
  name,
  writeOffDate,
});

const cards: Card[] = [
  makeCard("0003", "Laboratorní skříň"),
  makeCard("0001", "Studentská lavice vestavěná"),
  makeCard("0002", "Počítač stolní", new Date("2024-01-01")),
  makeCard("0004", "Židle kancelářská"),
];

describe("useCardSearch", () => {
  const defaultSort = { key: "id" as keyof Card, direction: "asc" as const };

  it("returns all non-written-off cards when search is empty", () => {
    const { result } = renderHook(() => useCardSearch(cards, "", false, defaultSort));
    // Card 0002 is written off → should be excluded
    expect(result.current).toHaveLength(3);
    expect(result.current.map((c) => c.id)).not.toContain("0002");
  });

  it("includes written-off cards when showWrittenOff is true", () => {
    const { result } = renderHook(() => useCardSearch(cards, "", true, defaultSort));
    expect(result.current).toHaveLength(4);
    expect(result.current.map((c) => c.id)).toContain("0002");
  });

  it("sorts by id ascending", () => {
    const { result } = renderHook(() => useCardSearch(cards, "", true, { key: "id", direction: "asc" }));
    expect(result.current.map((c) => c.id)).toEqual(["0001", "0002", "0003", "0004"]);
  });

  it("sorts by id descending", () => {
    const { result } = renderHook(() => useCardSearch(cards, "", true, { key: "id", direction: "desc" }));
    expect(result.current.map((c) => c.id)).toEqual(["0004", "0003", "0002", "0001"]);
  });

  it("sorts by name ascending with Czech collation", () => {
    const { result } = renderHook(() => useCardSearch(cards, "", true, { key: "name", direction: "asc" }));
    const names = result.current.map((c) => c.name);
    // Czech alphabetical: L < P < S < Ž
    expect(names).toEqual(["Laboratorní skříň", "Počítač stolní", "Studentská lavice vestavěná", "Židle kancelářská"]);
  });

  it("searches by exact name match (accent-insensitive)", () => {
    const { result } = renderHook(() => useCardSearch(cards, "laboratorni", false, defaultSort));
    expect(result.current).toHaveLength(1);
    expect(result.current[0].name).toBe("Laboratorní skříň");
  });

  it("searches by ID", () => {
    const { result } = renderHook(() => useCardSearch(cards, "0004", true, defaultSort));
    expect(result.current).toHaveLength(1);
    expect(result.current[0].id).toBe("0004");
  });

  it("returns empty array when search matches nothing", () => {
    const { result } = renderHook(() => useCardSearch(cards, "neexistuje", true, defaultSort));
    expect(result.current).toHaveLength(0);
  });

  it("combines search with write-off filter", () => {
    // "pocitac" matches card 0002 which is written off
    const { result: hidden } = renderHook(() => useCardSearch(cards, "pocitac", false, defaultSort));
    expect(hidden.current).toHaveLength(0);

    const { result: shown } = renderHook(() => useCardSearch(cards, "pocitac", true, defaultSort));
    expect(shown.current).toHaveLength(1);
  });
});
