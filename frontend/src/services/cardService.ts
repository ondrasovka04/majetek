import authorizedRequest from "../utils/authorizedRequest";
import { Card } from "../types/card/card";
import { CardFilterQuery } from "../types/card/cardFilterQuery";
import { saveAs } from "file-saver";
import { CardDetail } from "../types/card/cardDetail";

export const getCards = async (): Promise<Card[]> => {
  const res = await authorizedRequest.get("/card");
  return res.data.map((card: any) => ({
    ...card,
    id: card.id.toString().padStart(4, "0"),
  }));
};

export const filterCards = async (
  filters: CardFilterQuery
): Promise<Card[]> => {
  const params: Record<string, string> = {};

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      if (value instanceof Date) {
        params[key] = value.toLocaleDateString("sv-SE");
      } else {
        params[key] = String(value);
      }
    }
  });

  const res = await authorizedRequest.get("/card/filter", { params });
  return res.data.map((card: any) => ({
    ...card,
    id: card.id.toString().padStart(4, "0"),
  }));
};

export const printCards = async (
  query: CardFilterQuery,
  cloudPrint: boolean
): Promise<string | void> => {
  const res = await authorizedRequest.post(
    `/card/print?printCloud=${cloudPrint}`,
    query
  );

  if (!cloudPrint) {
    const blob = new Blob([res.data], { type: "application/octet-stream" });
    saveAs(blob, "label.zpl");
    return;
  }

  return "Štítek byl úspěšně odeslán do tiskárny.";
};

export const exportCardsExcel = async (
  query: CardFilterQuery
): Promise<void> => {
  const res = await authorizedRequest.post("/card/export-excel", query, {
    responseType: "blob",
  });

  const blob = new Blob([res.data], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  saveAs(blob, "cards.xlsx");
};

export const exportGroupPdf = async (
  groupId: string,
  selectedDate: Date | null
): Promise<string> => {
  const response = await authorizedRequest.post(
    `/card/export-group-pdf`,
    {
      groupId,
      asOf: selectedDate
        ? selectedDate.toLocaleDateString("sv-SE")
        : new Date().toLocaleDateString("sv-SE"),
      allGroups: groupId ? false : true,
    },
    {
      responseType: "blob",
    }
  );

  const blob = new Blob([response.data], { type: "application/pdf" });
  return URL.createObjectURL(blob);
};

export const exportLocationPdf = async (
  locationId: string,
  selectedDate: Date | null
): Promise<string> => {
  const response = await authorizedRequest.post(
    `/card/export-location-pdf`,
    {
      locationId,
      asOf: selectedDate
        ? selectedDate.toLocaleDateString("sv-SE")
        : new Date().toLocaleDateString("sv-SE"),
    },
    {
      responseType: "blob",
    }
  );

  const blob = new Blob([response.data], { type: "application/pdf" });
  return URL.createObjectURL(blob);
};

export const exportCardPdf = async (cardId: number): Promise<{ blob: Blob; url: string }> => {
  const response = await authorizedRequest.post(
    `/card/export-card-pdf`,
    {
      cardId,
    },
    {
      responseType: "blob",
    }
  );

  const blob = new Blob([response.data], { type: "application/pdf" });
  return { url: URL.createObjectURL(blob), blob };
};

export const getCardById = async (id: string): Promise<CardDetail> => {
  const res = await authorizedRequest.get(`/card/${id}`);
  const card = res.data;
  return {
    ...card,
    id: card.id.toString().padStart(4, "0"),
    purchaseDate: new Date(card.purchaseDate),
    writeOffDate: card.writeOffDate ? new Date(card.writeOffDate) : null,
    createdDate: new Date(card.createdDate),
  };
};

export const updateCard = async (
  id: string,
  updatedCard: CardDetail
): Promise<void> => {
  await authorizedRequest.patch(`/card/${id}`, {
    name: updatedCard.name,
    price: updatedCard.price ?? undefined,
    quantity: updatedCard.quantity ?? undefined,
    serialNumber: updatedCard.serialNumber || undefined,
    locationId: updatedCard.locationId,
    classId: updatedCard.classId || undefined,
    groupId: updatedCard.groupId || undefined,
    purchaseDate: updatedCard.purchaseDate.toLocaleDateString("sv-SE"),
    purchaseMethodId: updatedCard.purchaseMethodId || undefined,
    purchaseInvoice: updatedCard.purchaseInvoice || undefined,
    writeOffDate: updatedCard.writeOffDate
      ? updatedCard.writeOffDate.toLocaleDateString("sv-SE")
      : undefined,
    writeOffMethodId: updatedCard.writeOffMethodId || undefined,
    createdById: updatedCard.createdById || undefined,
    responsiblePersonId: updatedCard.responsiblePersonId || undefined,
    note: updatedCard.note || undefined,
  });
};

export const createCard = async (card: Partial<CardDetail>): Promise<void> => {
  await authorizedRequest.post("/card", {
    name: card.name,
    price: card.price ?? undefined,
    quantity: card.quantity ?? undefined,
    serialNumber: card.serialNumber || undefined,
    locationId: card.locationId,
    classId: card.classId || undefined,
    groupId: card.groupId || undefined,
    purchaseDate: card.purchaseDate?.toLocaleDateString("sv-SE"),
    purchaseMethodId: card.purchaseMethodId || undefined,
    purchaseInvoice: card.purchaseInvoice || undefined,
    writeOffDate: card.writeOffDate
      ? card.writeOffDate.toLocaleDateString("sv-SE")
      : undefined,
    writeOffMethodId: card.writeOffMethodId || undefined,
    createdById: card.createdById || undefined,
    responsiblePersonId: card.responsiblePersonId || undefined,
    note: card.note || undefined,
  });
};

export const deleteCard = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/card/${id}`);
};
