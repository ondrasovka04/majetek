import { describe, it, expect } from "vite-plus/test";
import { normalizeString, sortById, czechCollator, czechCollatorBase } from "@/utils/czech";

describe("normalizeString", () => {
  it("strips Czech diacritics", () => {
    expect(normalizeString("příliš žluťoučký kůň")).toBe("prilis zlutoucky kun");
  });

  it("returns plain ASCII unchanged", () => {
    expect(normalizeString("hello world")).toBe("hello world");
  });

  it("handles empty string", () => {
    expect(normalizeString("")).toBe("");
  });

  it("strips combined diacritics (ě, ř, ů, ď, ť, ň)", () => {
    expect(normalizeString("ěščřžýáíéúůďťň")).toBe("escrzyaieuudtn");
  });
});

describe("sortById", () => {
  it("sorts items by id in Czech locale order", () => {
    const items = [
      { id: "C01", name: "c" },
      { id: "A01", name: "a" },
      { id: "B01", name: "b" },
    ];
    const sorted = sortById(items);
    expect(sorted.map((i) => i.id)).toEqual(["A01", "B01", "C01"]);
  });

  it("does not mutate original array", () => {
    const items = [{ id: "B" }, { id: "A" }];
    const sorted = sortById(items);
    expect(sorted).not.toBe(items);
    expect(items[0].id).toBe("B");
  });

  it("handles numeric IDs as strings", () => {
    const items = [{ id: "0010" }, { id: "0002" }, { id: "0001" }];
    const sorted = sortById(items);
    expect(sorted.map((i) => i.id)).toEqual(["0001", "0002", "0010"]);
  });

  it("handles empty array", () => {
    expect(sortById([])).toEqual([]);
  });

  it("accepts a custom collator", () => {
    const items = [{ id: "č" }, { id: "c" }, { id: "d" }];
    // czechCollatorBase (sensitivity: "base") treats č ≈ c
    const sortedBase = sortById(items, czechCollatorBase);
    // czechCollator (sensitivity: "variant") distinguishes č from c
    const sortedVariant = sortById(items, czechCollator);
    // Both should put "d" last in Czech
    expect(sortedBase.at(-1)?.id).toBe("d");
    expect(sortedVariant.at(-1)?.id).toBe("d");
  });
});

describe("czechCollatorBase", () => {
  it("ignores case differences (base sensitivity)", () => {
    expect(czechCollatorBase.compare("A", "a")).toBe(0);
    expect(czechCollatorBase.compare("Č", "č")).toBe(0);
  });

  it("still distinguishes č from c (separate Czech letters)", () => {
    // č and c are distinct letters in Czech alphabet, not accent variants
    expect(czechCollatorBase.compare("č", "c")).not.toBe(0);
  });
});

describe("czechCollator", () => {
  it("distinguishes accented characters (variant sensitivity)", () => {
    expect(czechCollator.compare("č", "c")).not.toBe(0);
    expect(czechCollator.compare("š", "s")).not.toBe(0);
  });

  it("sorts Czech characters in correct order (ch after h)", () => {
    // In Czech: c < č < d < ... < h < ch < i
    expect(czechCollator.compare("č", "d")).toBeLessThan(0);
    expect(czechCollator.compare("h", "ch")).toBeLessThan(0);
    expect(czechCollator.compare("ch", "i")).toBeLessThan(0);
  });
});
