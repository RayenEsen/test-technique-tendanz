import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { QuoteResponse, QuoteHistoryEntry } from '../../models/quote.model';

@Component({
  selector: 'app-quote-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quote-detail.component.html',
  styleUrl: './quote-detail.component.css'
})
export class QuoteDetailComponent implements OnInit {
  quote: QuoteResponse | null = null;
  history: QuoteHistoryEntry[] = [];
  loading = false;
  pdfLoading = false;
  errorMessage: string | null = null;

  constructor(
    private quoteService: QuoteService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Invalid quote ID';
      return;
    }
    this.loading = true;
    this.quoteService.getQuote(id).subscribe({
      next: (quote) => {
        this.quote = quote;
        this.loading = false;
        this.loadHistory(id);
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.loading = false;
      }
    });
  }

  loadHistory(id: number): void {
    this.quoteService.getQuoteHistory(id).subscribe({
      next: (history) => { this.history = history; },
      error: () => { /* history is bonus, fail silently */ }
    });
  }

  downloadPdf(): void {
    if (!this.quote) return;
    this.pdfLoading = true;
    this.quoteService.exportPdf(this.quote.quoteId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `quote-${this.quote!.quoteId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.pdfLoading = false;
      },
      error: () => {
        this.pdfLoading = false;
      }
    });
  }
}
