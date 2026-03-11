import { describe, it, expect } from "vitest";
import { translateError } from "@/utils/errorTranslations";

describe("translateError", () => {
  // --- Regex patterns ---

  it("translates 'Location with id X already exists'", () => {
    expect(translateError("Location with id L01 already exists")).toBe("Umístění s ID L01 již existuje.");
  });

  it("translates 'Asset group with id X already exists'", () => {
    expect(translateError("Asset group with id G01 already exists")).toBe("Skupina s ID G01 již existuje.");
  });

  it("translates 'Class with id X already exists'", () => {
    expect(translateError("Class with id C01 already exists")).toBe("Třída s ID C01 již existuje.");
  });

  it("translates 'Purchase method with id X already exists'", () => {
    expect(translateError("Purchase method with id PM1 already exists")).toBe("Způsob pořízení s ID PM1 již existuje.");
  });

  it("translates 'Write off method with id X already exists'", () => {
    expect(translateError("Write off method with id WO1 already exists")).toBe(
      "Způsob vyřazení s ID WO1 již existuje.",
    );
  });

  it("translates 'Printing more than N cards is forbidden'", () => {
    expect(translateError("Printing more than 50 cards is forbidden")).toBe("Tisk více než 50 štítků není povolen.");
  });

  // --- Static translations ---

  it("translates 'Invalid username or password'", () => {
    expect(translateError("Invalid username or password")).toBe("Špatné přihlašovací údaje");
  });

  it("translates 'Maximum upload size exceeded'", () => {
    expect(translateError("Maximum upload size exceeded")).toBe("Příloha je příliš velká");
  });

  it("translates 'Validation failed'", () => {
    expect(translateError("Validation failed")).toBe("Chyba validace vstupu");
  });

  // --- Fallbacks ---

  it("returns original message if no translation found", () => {
    expect(translateError("Some unknown error")).toBe("Some unknown error");
  });

  it("returns 'Neznámá chyba.' for empty string", () => {
    expect(translateError("")).toBe("Neznámá chyba.");
  });
});
