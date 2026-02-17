/** Full asset card detail returned by the API for view/edit screens. */
export interface CardDetail {
  id: string;
  name: string;
  price: number;
  quantity: number;
  serialNumber: string | null;
  locationId: string;
  classId: string;
  groupId: string;
  purchaseDate: Date;
  purchaseMethodId: string;
  purchaseInvoice: string | null;
  writeOffDate: Date | null;
  writeOffMethodId: string | null;
  createdById: string;
  createdDate: Date;
  responsiblePersonId: string;
  note: string;
}
