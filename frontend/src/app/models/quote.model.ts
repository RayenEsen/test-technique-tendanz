export interface QuoteRequest {
  productId: number;
  zoneCode: string;
  clientName: string;
  clientAge: number;
}

export interface QuoteResponse {
  quoteId: number;
  productName: string;
  zoneName: string;
  clientName: string;
  clientAge: number;
  basePrice: number;
  finalPrice: number;
  appliedRules: string[];
  createdAt: string;
}

export interface PagedQuoteResponse {
  content: QuoteResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface QuoteHistoryEntry {
  id: number;
  quoteId: number;
  changedAt: string;
  changeType: string;
  previousFinalPrice: number | null;
  newFinalPrice: number | null;
  previousZoneCode: string | null;
  newZoneCode: string | null;
  previousProductName: string | null;
  newProductName: string | null;
  changeDescription: string;
}
