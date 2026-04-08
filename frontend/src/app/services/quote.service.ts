import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { QuoteRequest, QuoteResponse } from '../models/quote.model';

/**
 * Service for managing quotes
 * This service handles all API communication with the backend pricing engine
 *
 * TODO: Candidate must implement the following methods:
 * - createQuote(request: QuoteRequest): Observable<QuoteResponse>
 * - getQuote(id: number): Observable<QuoteResponse>
 * - getQuotes(filters?: {productId?: number, minPrice?: number}): Observable<QuoteResponse[]>
 *
 * Requirements:
 * - Use HttpClient for HTTP requests
 * - Use catchError operator to handle errors
 * - Base URL should be configurable via environment.apiUrl
 * - Handle error responses appropriately (log errors, throw user-friendly messages)
 */
@Injectable({
  providedIn: 'root'
})
export class QuoteService {
  private readonly apiUrl = environment.apiUrl;
  private readonly endpoint = '/quotes';

  constructor(private http: HttpClient) {}

  /**
   * Create a new quote
   * POST /api/quotes
   *
   * @param request Quote request data
   * @returns Observable of the created quote response with calculated pricing
   *
   * TODO: Implement this method
   */
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

  private handleError(error: any): Observable<never> {
    console.error('Quote service error:', error);
    const message = error?.error?.message || 'Failed to process quote';
    return throwError(() => new Error(message));
  }
}
