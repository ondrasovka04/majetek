/** API service functions for asset cards — CRUD, filtering, printing, and PDF/Excel export. */
import authorizedRequest from "../utils/authorizedRequest";
import { Card } from "../types/card/card";
import { CardFilterQuery } from "../types/card/cardFilterQuery";
import { saveAs } from "file-saver";
import { CardDetail } from "../types/card/cardDetail";
import { CardFormOutput } from "../schemas/cardSchema";
import { toIsoDateString } from "../utils/formatDate";

export const getCards = async (): Promise<Card[]> => {
  const res = await authorizedRequest.get("/card");
  return res.data.map((card: { id: number; name: string; writeOffDate: Date | null }) => ({
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
        params[key] = toIsoDateString(value)!;
      } else {
        params[key] = String(value);
      }
    }
  });

  const res = await authorizedRequest.get("/card/filter", { params });
  return res.data.map((card: { id: number; name: string; writeOffDate: Date | null }) => ({
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
      asOf: toIsoDateString(selectedDate ?? new Date()),
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
      asOf: toIsoDateString(selectedDate ?? new Date()),
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
  data: CardFormOutput
): Promise<void> => {
  await authorizedRequest.patch(`/card/${id}`, {
    name: data.name,
    price: data.price ?? undefined,
    quantity: data.quantity ?? undefined,
    serialNumber: data.serialNumber ?? undefined,
    locationId: data.locationId,
    classId: data.classId ?? undefined,
    groupId: data.groupId ?? undefined,
    purchaseDate: toIsoDateString(data.purchaseDate),
    purchaseMethodId: data.purchaseMethodId ?? undefined,
    purchaseInvoice: data.purchaseInvoice ?? undefined,
    writeOffDate: toIsoDateString(data.writeOffDate) ?? undefined,
    writeOffMethodId: data.writeOffMethodId ?? undefined,
    createdById: data.createdById ?? undefined,
    responsiblePersonId: data.responsiblePersonId ?? undefined,
    note: data.note ?? undefined,
  });
};

export const createCard = async (data: CardFormOutput): Promise<void> => {
  await authorizedRequest.post("/card", {
    name: data.name,
    price: data.price ?? undefined,
    quantity: data.quantity ?? undefined,
    serialNumber: data.serialNumber ?? undefined,
    locationId: data.locationId,
    classId: data.classId ?? undefined,
    groupId: data.groupId ?? undefined,
    purchaseDate: toIsoDateString(data.purchaseDate) ?? undefined,
    purchaseMethodId: data.purchaseMethodId ?? undefined,
    purchaseInvoice: data.purchaseInvoice ?? undefined,
    writeOffDate: toIsoDateString(data.writeOffDate) ?? undefined,
    writeOffMethodId: data.writeOffMethodId ?? undefined,
    createdById: data.createdById ?? undefined,
    responsiblePersonId: data.responsiblePersonId ?? undefined,
    note: data.note ?? undefined,
  });
};

export const deleteCard = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/card/${id}`);
};
