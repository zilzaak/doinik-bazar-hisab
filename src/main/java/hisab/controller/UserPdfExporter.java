package hisab.controller;

import hisab.entity.Market;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class UserPdfExporter {
    private final List<Market> listNobis;
    private final Double total;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    public UserPdfExporter(List<Market> listNobis, Double total) {
        this.listNobis = listNobis;
        this.total = total;
    }

    public void export(HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 0, 128));
        Paragraph title = new Paragraph("Shopping Summary Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15);
        document.add(title);

        // Summary Section
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph summary = new Paragraph();
        summary.setAlignment(Element.ALIGN_CENTER);
        summary.setSpacingAfter(20);

        Chunk itemsChunk = new Chunk("Total Items: " + listNobis.size() + "    ", summaryFont);
        itemsChunk.setBackground(new BaseColor(230, 240, 255)); // Light blue

        Chunk totalChunk = new Chunk(String.format("Total Price: ₹%.2f", total), summaryFont);
        totalChunk.setBackground(new BaseColor(230, 240, 255)); // Light blue

        summary.add(itemsChunk);
        summary.add(totalChunk);
        document.add(summary);

        // Generation date
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(128, 128, 128));
        Paragraph datePara = new Paragraph("Generated on: " + LocalDate.now().format(formatter), dateFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingAfter(15);
        document.add(datePara);

        // Create table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3.5f, 2f, 2.5f});

        // Table Header - Navy Blue with White Text
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        BaseColor navyBlue = new BaseColor(0, 0, 128);

        String[] headers = {"SL", "Item Name", "Item Price (₹)", "Date"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(navyBlue);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(8);
            table.addCell(headerCell);
        }

        // Table Data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font priceFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        int slNo = 1;
        for (Market item : listNobis) {
            // SL No
            PdfPCell slCell = new PdfPCell(new Phrase(String.valueOf(slNo++), dataFont));
            slCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            slCell.setPadding(6);
            table.addCell(slCell);

            // Item Name
            PdfPCell nameCell = new PdfPCell(new Phrase(item.getItemName(), dataFont));
            nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            nameCell.setPadding(6);
            table.addCell(nameCell);

            // Item Price
            PdfPCell priceCell = new PdfPCell(new Phrase(String.format("₹%.2f", item.getItemPrice()), priceFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setPadding(6);
            table.addCell(priceCell);

            // Date
            PdfPCell dateCell = new PdfPCell(new Phrase(item.getDate().format(formatter), dataFont));
            dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dateCell.setPadding(6);
            table.addCell(dateCell);
        }

        document.add(table);

        // Total Row
        if (!listNobis.isEmpty()) {
            PdfPTable totalTable = new PdfPTable(4);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{1f, 3.5f, 2f, 2.5f});
            totalTable.setSpacingBefore(10);

            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            BaseColor lightBlue = new BaseColor(230, 240, 255);

            // TOTAL label spanning first two columns
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
            totalLabelCell.setColspan(2);
            totalLabelCell.setBackgroundColor(lightBlue);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalLabelCell.setPadding(6);
            totalTable.addCell(totalLabelCell);

            // Total value
            PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("₹%.2f", total), totalFont));
            totalValueCell.setBackgroundColor(lightBlue);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setPadding(6);
            totalTable.addCell(totalValueCell);

            // Empty cell
            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBackgroundColor(lightBlue);
            emptyCell.setPadding(6);
            totalTable.addCell(emptyCell);

            document.add(totalTable);
        }

        document.close();
    }
}