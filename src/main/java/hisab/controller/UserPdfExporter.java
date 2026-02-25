package hisab.controller;

import hisab.entity.Market;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class UserPdfExporter {

    private final List<Market> listNobis;
    private final Double total;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    public String getFileName(List<Market> listNobis){
        String fileName="Shopping_Summary_from_"+listNobis.get(listNobis.size()-1).getDate().format(formatter)+"_To_"+listNobis.get(0).getDate().format(formatter)+".pdf";
        return fileName;
    }
    public UserPdfExporter(List<Market> listNobis, Double total) {
        this.listNobis = listNobis;
        this.total = total;
    }

    // ✅ COMMON METHOD → used by both download and email
    private void writeContent(Document document) throws DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

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

        Font paramFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph paramParagraph= new Paragraph();
        paramParagraph.setAlignment(Element.ALIGN_CENTER);
        paramParagraph.setSpacingAfter(20);

        Chunk itemsChunk = new Chunk("Total Items: " + listNobis.size() + "    ", summaryFont);
        itemsChunk.setBackground(new BaseColor(230, 240, 255));

        Chunk totalChunk = new Chunk(String.format("Total Price: %.2f BDT", total), summaryFont);
        totalChunk.setBackground(new BaseColor(230, 240, 255));

        String to=listNobis.get(0).getDate().format(formatter);
        String from =  listNobis.get(listNobis.size()-1).getDate().format(formatter);

        Chunk paramsChunk = new Chunk("From : "+from+"  To : "+to, paramFont);
        paramsChunk.setBackground(new BaseColor(230, 240, 255));

        summary.add(itemsChunk);
        summary.add(totalChunk);
        paramParagraph.add(paramsChunk);
        document.add(summary);
        document.add(paramParagraph);

        // Generation date
        Font dateFont = FontFactory.getFont(
                FontFactory.HELVETICA, 10, new BaseColor(128, 128, 128));
        Paragraph datePara = new Paragraph(
                "Generated on: " + LocalDate.now().format(formatter), dateFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingAfter(15);
        document.add(datePara);

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f,2f, 3.5f, 2.5f});

        Font headerFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        BaseColor navyBlue = new BaseColor(0, 0, 128);

        String[] headers = {"SL","Date","Item Name", "Item Price" };
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(navyBlue);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(8);
            table.addCell(headerCell);
        }

        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        int slNo = 1;
        for (Market item : listNobis) {

            PdfPCell slCell = new PdfPCell(new Phrase(String.valueOf(slNo++), dataFont));
            slCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            slCell.setPadding(6);
            table.addCell(slCell);

            PdfPCell dateCell = new PdfPCell(new Phrase(item.getDate().format(formatter)+" , "+item.getDate().getDayOfWeek().name().substring(0, 3), dataFont));
            dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dateCell.setPadding(6);
            table.addCell(dateCell);

            PdfPCell nameCell = new PdfPCell(new Phrase(item.getItemName(), dataFont));
            nameCell.setPadding(6);
            table.addCell(nameCell);

            PdfPCell priceCell = new PdfPCell(new Phrase(String.format("%.2f", item.getItemPrice()), dataFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setPadding(6);
            table.addCell(priceCell);

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

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
            totalLabelCell.setColspan(2);
            totalLabelCell.setBackgroundColor(lightBlue);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalLabelCell.setPadding(6);
            totalTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(
                            String.format("%.2f BDT", total), totalFont));
            totalValueCell.setBackgroundColor(lightBlue);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setPadding(6);
            totalTable.addCell(totalValueCell);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBackgroundColor(lightBlue);
            emptyCell.setPadding(6);
            totalTable.addCell(emptyCell);

            document.add(totalTable);
        }
    }

    // ✅ FOR BROWSER DOWNLOAD
    public void export(HttpServletResponse response)
            throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        writeContent(document);
        document.close();
    }

    // ✅ FOR EMAIL ATTACHMENT
    public byte[] exportToByteArray()
            throws IOException, DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();
        writeContent(document);
        document.close();
        return outputStream.toByteArray();
    }
}