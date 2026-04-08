import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { QuoteRequest, QuoteResponse, PagedQuoteResponse, QuoteHistoryEntry } from '../models/quote.model';

@Injectable({
  providedIn: 'root'
})
export class QuoteService {
  private readonly apiUrl = environment.apiUrl;
  private readonly endpoint = '/quotes';

  constructor(private http: HttpClient) {}

  createQuote(request: QuoteRequest): Observable<QuoteResponse> {
    return this.http.post<QuoteResponse>(`${this.apiUrl}${this.endpoint}`, request)
      .pipe(catchError(this.handleError));
  }

  getQuote(id: number): Observable<QuoteResponse> {
    return this.http.get<QuoteResponse>(`${this.apiUrl}${this.endpoint}/${id}`)
      .pipe(catchError(this.handleError));
  }

  getQuotes(filters?: { productId?: number; minPrice?: number }): Observable<QuoteResponse[]> {
    let params = new HttpParams();
    if (filters?.productId) params = params.set('productId', filters.productId);
    if (filters?.minPrice) params = params.set('minPrice', filters.minPrice);
    return this.http.get<QuoteResponse[]>(`${this.apiUrl}${this.endpoint}`, { params })
      .pipe(catchError(this.handleError));
  }

  getQuotesPaged(filters?: { productId?: number; minPrice?: number }, page = 0, size = 10): Observable<PagedQuoteResponse> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filters?.productId) params = params.set('productId', filters.productId);
    if (filters?.minPrice) params = params.set('minPrice', filters.minPrice);
    return this.http.get<PagedQuoteResponse>(`${this.apiUrl}${this.endpoint}`, { params })
      .pipe(catchError(this.handleError));
  }

  getQuoteHistory(id: number): Observable<QuoteHistoryEntry[]> {
    return this.http.get<QuoteHistoryEntry[]>(`${this.apiUrl}${this.endpoint}/${id}/history`)
      .pipe(catchError(this.handleError));
  }

  exportPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}${this.endpoint}/${id}/pdf`, { responseType: 'blob' })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('Quote service error:', error);
    const message = error?.error?.message || 'Failed to process quote';
    return throwError(() => new Error(message));
  }
}
