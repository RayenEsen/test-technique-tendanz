import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuoteService } from '../../services/quote.service';
import { ProductService } from '../../services/product.service';
import { QuoteResponse } from '../../models/quote.model';
import { Product } from '../../models/product.model';

/**
 * Component for displaying a list of all quotes
 *
 * TODO: Candidate must implement the following:
 * 1. Load all quotes on component initialization using QuoteService.getQuotes()
 *
 * 2. Load products for filter dropdown using ProductService.getProducts()
 *
 * 3. Implement filtering in applyFilters():
 *    - Build filter object from selectedProductId and minPrice
 *    - Call QuoteService.getQuotes(filters)
 *    - Update filteredQuotes with results
 *
 * 4. Implement sorting in sortQuotes():
 *    - Sort by creation date (ascending/descending)
 *    - Sort by final price (ascending/descending)
 *
 * 5. Implement viewQuote() to navigate to /quotes/:id
 *
 * 6. Handle loading and error states
 */
@Component({
  selector: 'app-quote-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './quote-list.component.html',
  styleUrl: './quote-list.component.css'
})
export class QuoteListComponent implements OnInit {
  quotes: QuoteResponse[] = [];
  filteredQuotes: QuoteResponse[] = [];
  products: Product[] = [];
  loading = false;
  errorMessage: string | null = null;

  // Filter state
  selectedProductId: number | null = null;
  minPrice: number | null = null;

  // Sort state
  sortField: 'date' | 'price' = 'date';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(
    private quoteService: QuoteService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.productService.getProducts().subscribe({
      next: (products) => { this.products = products; },
      error: (err) => { this.errorMessage = err.message; }
    });
    this.quoteService.getQuotes().subscribe({
      next: (quotes) => {
        this.quotes = quotes;
        this.filteredQuotes = [...quotes];
        this.sortQuotes();
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    const filters: { productId?: number; minPrice?: number } = {};
    if (this.selectedProductId) filters.productId = this.selectedProductId;
    if (this.minPrice) filters.minPrice = this.minPrice;

    this.loading = true;
    this.quoteService.getQuotes(filters).subscribe({
      next: (quotes) => {
        this.filteredQuotes = quotes;
        this.sortQuotes();
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.selectedProductId = null;
    this.minPrice = null;
    this.applyFilters();
  }

  /**
   * Toggle sort direction or change sort field
   */
  changeSortField(field: 'date' | 'price'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.sortQuotes();
  }

  /**
   * Sort filteredQuotes in memory
   *
   * TODO: Implement sorting
   * - For 'date': sort by createdAt
   * - For 'price': sort by finalPrice
   * - Apply sortDirection (asc/desc)
   */
  private sortQuotes(): void {
    this.filteredQuotes.sort((a, b) => {
      let cmp = 0;
      if (this.sortField === 'date') {
        cmp = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      } else {
        cmp = a.finalPrice - b.finalPrice;
      }
      return this.sortDirection === 'asc' ? cmp : -cmp;
    });
  }

  viewQuote(id: number): void {
    this.router.navigate(['/quotes', id]);
  }
}
