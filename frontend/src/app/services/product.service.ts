import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Product } from '../models/product.model';

/**
 * Service for managing products (insurance products)
 *
 * TODO: Candidate must implement the following method:
 * - getProducts(): Observable<Product[]>
 *
 * Requirements:
 * - Use HttpClient for HTTP requests
 * - Use catchError operator to handle errors
 * - Base URL should be configurable via environment.apiUrl
 */
@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly apiUrl = environment.apiUrl;
  private readonly endpoint = '/products';

  constructor(private http: HttpClient) {}

  /**
   * Get all available products
   * GET /api/products
   *
   * @returns Observable of array of products
   *
   * TODO: Implement this method
   * - GET from ${this.apiUrl}${this.endpoint}
   * - Handle errors with catchError
   */
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}${this.endpoint}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('Product service error:', error);
    return throwError(() => new Error('Failed to load products'));
  }
}
