import { describe, it, expect } from "vite-plus/test";
import { toIsoDateString } from "@/utils/formatDate";

describe("toIsoDateString", () => {
  it("formats a Date object to yyyy-MM-dd", () => {
    expect(toIsoDateString(new Date(2025, 0, 15))).toBe("2025-01-15");
  });

  it("returns an ISO string as-is", () => {
    expect(toIsoDateString("2024-06-01")).toBe("2024-06-01");
  });

  it("returns null for null input", () => {
    expect(toIsoDateString(null)).toBeNull();
  });

  it("returns null for undefined input", () => {
    expect(toIsoDateString(undefined)).toBeNull();
  });

  it("formats end-of-year date correctly", () => {
    expect(toIsoDateString(new Date(2025, 11, 31))).toBe("2025-12-31");
  });
});
