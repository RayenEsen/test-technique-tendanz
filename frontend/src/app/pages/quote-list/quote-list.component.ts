import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuoteService } from '../../services/quote.service';
import { ProductService } from '../../services/product.service';
import { QuoteResponse } from '../../models/quote.model';
import { Product } from '../../models/product.model';

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

  // Pagination state
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  isLastPage = false;

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
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading = true;
    const filters: { productId?: number; minPrice?: number } = {};
    if (this.selectedProductId) filters.productId = this.selectedProductId;
    if (this.minPrice) filters.minPrice = this.minPrice;

    this.quoteService.getQuotesPaged(filters, page, this.pageSize).subscribe({
      next: (paged) => {
        this.filteredQuotes = paged.content;
        this.currentPage = paged.page;
        this.totalElements = paged.totalElements;
        this.totalPages = paged.totalPages;
        this.isLastPage = paged.last;
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
    this.loadPage(0);
  }

  resetFilters(): void {
    this.selectedProductId = null;
    this.minPrice = null;
    this.loadPage(0);
  }

  nextPage(): void {
    if (!this.isLastPage) this.loadPage(this.currentPage + 1);
  }

  prevPage(): void {
    if (this.currentPage > 0) this.loadPage(this.currentPage - 1);
  }

  changeSortField(field: 'date' | 'price'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.sortQuotes();
  }

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
