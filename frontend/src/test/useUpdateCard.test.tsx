import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import React from "react";
import { useUpdateCard } from "@/hooks/queries/useCardQueries";
import type { CardFormOutput } from "@/schemas/cardSchema";

vi.mock("@/services/cardService", () => ({
  updateCard: vi.fn().mockResolvedValue(undefined),
}));

const DUMMY_CARD: CardFormOutput = {
  name: "Test",
  price: null,
  quantity: null,
  serialNumber: null,
  locationId: "LOC1",
  groupId: "GRP1",
  classId: null,
  purchaseMethodId: null,
  purchaseDate: new Date("2025-01-01"),
  purchaseInvoice: null,
  writeOffDate: null,
  writeOffMethodId: null,
  responsiblePersonId: null,
  note: null,
};

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={qc}>{children}</QueryClientProvider>
  );
  return { qc, wrapper };
}

describe("useUpdateCard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("invalidates both cards list and card detail on success", async () => {
    const { qc, wrapper } = createWrapper();
    const invalidateSpy = vi.spyOn(qc, "invalidateQueries");

    const { result } = renderHook(() => useUpdateCard(), { wrapper });

    result.current.mutate({ id: "0042", card: DUMMY_CARD });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ["cards"] });
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ["card", "0042"] });
  });

  it("does not invalidate on error", async () => {
    const { updateCard } = await import("@/services/cardService");
    vi.mocked(updateCard).mockRejectedValueOnce(new Error("fail"));

    const { qc, wrapper } = createWrapper();
    const invalidateSpy = vi.spyOn(qc, "invalidateQueries");

    const { result } = renderHook(() => useUpdateCard(), { wrapper });

    result.current.mutate({ id: "0042", card: DUMMY_CARD });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(invalidateSpy).not.toHaveBeenCalled();
  });
});
