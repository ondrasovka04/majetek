/** Query parameters for filtering asset cards on the server. */
export interface CardFilterQuery {
  id?: number;
  minId?: number;
  maxId?: number;
  name?: string;
  minPrice?: number;
  maxPrice?: number;
  minQuantity?: number;
  maxQuantity?: number;
  locationId?: string;
  classId?: string;
  groupId?: string;
  purchasedAfter?: Date;
  purchasedBefore?: Date;
  purchaseMethodId?: string;
  purchaseInvoice?: string;
  writeOffAfter?: Date;
  writeOffBefore?: Date;
  writeOffMethodId?: string;
  createdBy?: string;
  createdAfter?: Date;
  createdBefore?: Date;
  responsePerson?: string;
  note?: string;
  fulltextSearch?: string;
  includeWriteOff?: boolean;
}
