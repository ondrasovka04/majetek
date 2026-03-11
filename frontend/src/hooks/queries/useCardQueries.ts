/** TanStack Query hooks for asset card CRUD, filtering, printing, and PDF/Excel export. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getCards,
  getCardById,
  filterCards,
  createCard,
  updateCard,
  deleteCard,
  printCards,
  exportCardsExcel,
  exportCardPdf,
  exportGroupPdf,
  exportLocationPdf,
} from "@/services/cardService";
import { Card } from "@/types/card/card";
import { CardFormOutput } from "@/schemas/cardSchema";
import { CardFilterQuery } from "@/types/card/cardFilterQuery";
import { getUsers } from "@/services/authService";

const CARDS_KEY = ["cards"] as const;
const USERS_KEY = ["users"] as const;

export function useCards() {
  return useQuery({
    queryKey: CARDS_KEY,
    queryFn: getCards,
  });
}

export function useCardDetail(cardId: string | null) {
  return useQuery({
    queryKey: ["card", cardId],
    queryFn: () => getCardById(cardId!),
    enabled: !!cardId && cardId !== "NEW",
  });
}

export function useFilterCards() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (filters: CardFilterQuery) => filterCards(filters),
    onSuccess: (data) => {
      qc.setQueryData<Card[]>(CARDS_KEY, data);
    },
  });
}

export function useCreateCard() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (card: CardFormOutput) => createCard(card),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CARDS_KEY });
    },
  });
}

export function useUpdateCard() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, card }: { id: string; card: CardFormOutput }) => updateCard(id, card),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: CARDS_KEY });
      qc.invalidateQueries({ queryKey: ["card", id] });
    },
  });
}

export function useDeleteCard() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteCard(id),
    onSuccess: (_data, id) => {
      qc.setQueryData<Card[]>(CARDS_KEY, (old) => old?.filter((c) => c.id !== id));
    },
  });
}

export function usePrintCards() {
  return useMutation({
    mutationFn: ({ query, cloudPrint }: { query: CardFilterQuery; cloudPrint: boolean }) =>
      printCards(query, cloudPrint),
  });
}

export function useExportCardsExcel() {
  return useMutation({
    mutationFn: (query: CardFilterQuery) => exportCardsExcel(query),
  });
}

export function useExportCardPdf() {
  return useMutation({
    mutationFn: (cardId: number) => exportCardPdf(cardId),
  });
}

export function useExportGroupPdf() {
  return useMutation({
    mutationFn: ({ groupId, selectedDate }: { groupId: string; selectedDate: Date | null }) =>
      exportGroupPdf(groupId, selectedDate),
  });
}

export function useExportLocationPdf() {
  return useMutation({
    mutationFn: ({ locationId, selectedDate }: { locationId: string; selectedDate: Date | null }) =>
      exportLocationPdf(locationId, selectedDate),
  });
}

export function useUsers() {
  return useQuery({
    queryKey: USERS_KEY,
    queryFn: getUsers,
    staleTime: Infinity,
  });
}
