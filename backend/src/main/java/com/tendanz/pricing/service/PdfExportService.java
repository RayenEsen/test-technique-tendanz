package com.tendanz.pricing.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tendanz.pricing.dto.QuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating PDF exports of quotes.
 */
@Service
@Slf4j
public class PdfExportService {

    private static final Color TENDANZ_BLUE = new Color(31, 56, 100);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Generate a PDF document for the given quote.
     *
     * @param quote the quote to export
     * @return byte array of the generated PDF
     */
    public byte[] generateQuotePdf(QuoteResponse quote) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, quote);
            addClientSection(document, quote);
            addPricingSection(document, quote);
            addAppliedRules(document, quote);
            addFooter(document, quote);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF for quote {}", quote.getQuoteId(), e);
            throw new RuntimeException("Failed to generate PDF for quote " + quote.getQuoteId());
        }
    }

    private void addHeader(Document doc, QuoteResponse quote) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, TENDANZ_BLUE);
        Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.GRAY);

        Paragraph title = new Paragraph("TENDANZ GROUP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph subtitle = new Paragraph("Insurance Pricing Engine", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitle);

        doc.add(new Paragraph(" "));

        Font quoteFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
        Paragraph quoteTitle = new Paragraph("QUOTE #" + quote.getQuoteId(), quoteFont);
        quoteTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(quoteTitle);

        doc.add(new Paragraph(" "));
        addSeparator(doc);
        doc.add(new Paragraph(" "));
    }

    private void addClientSection(Document doc, QuoteResponse quote) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, TENDANZ_BLUE);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

        doc.add(new Paragraph("Client Information", sectionFont));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        addTableRow(table, "Client Name", quote.getClientName(), labelFont, valueFont);
        addTableRow(table, "Age", quote.getClientAge() + " years", labelFont, valueFont);
        addTableRow(table, "Product", quote.getProductName(), labelFont, valueFont);
        addTableRow(table, "Zone", quote.getZoneName(), labelFont, valueFont);
        addTableRow(table, "Created At", quote.getCreatedAt() != null ? quote.getCreatedAt().format(DATE_FMT) : "-", labelFont, valueFont);

        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addPricingSection(Document doc, QuoteResponse quote) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, TENDANZ_BLUE);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, TENDANZ_BLUE);

        doc.add(new Paragraph("Pricing Summary", sectionFont));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        addTableRow(table, "Base Price", quote.getBasePrice() + " TND", labelFont, valueFont);

        PdfPCell totalLabel = new PdfPCell(new Phrase("Final Price", totalFont));
        totalLabel.setBorder(Rectangle.BOX);
        totalLabel.setPadding(6);
        totalLabel.setBackgroundColor(new Color(230, 236, 245));

        PdfPCell totalValue = new PdfPCell(new Phrase(quote.getFinalPrice() + " TND", totalFont));
        totalValue.setBorder(Rectangle.BOX);
        totalValue.setPadding(6);
        totalValue.setBackgroundColor(new Color(230, 236, 245));

        table.addCell(totalLabel);
        table.addCell(totalValue);

        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addAppliedRules(Document doc, QuoteResponse quote) throws DocumentException {
        if (quote.getAppliedRules() == null || quote.getAppliedRules().isEmpty()) return;

        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, TENDANZ_BLUE);
        Font ruleFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

        doc.add(new Paragraph("Applied Pricing Rules", sectionFont));
        doc.add(new Paragraph(" "));

        for (String rule : quote.getAppliedRules()) {
            Paragraph p = new Paragraph("• " + rule, ruleFont);
            p.setIndentationLeft(15);
            doc.add(p);
        }
        doc.add(new Paragraph(" "));
    }

    private void addFooter(Document doc, QuoteResponse quote) throws DocumentException {
        addSeparator(doc);
        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
        Paragraph footer = new Paragraph("This document was generated automatically by Tendanz Group Pricing Engine.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private void addSeparator(Document doc) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthBottom(1f);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderColorBottom(TENDANZ_BLUE);
        cell.setPadding(0);
        line.addCell(cell);
        doc.add(line);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(new Color(245, 247, 250));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOX);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
