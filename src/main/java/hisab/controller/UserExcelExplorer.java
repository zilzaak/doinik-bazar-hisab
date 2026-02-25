package hisab.controller;

import hisab.entity.Market;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class UserExcelExplorer {
    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFSheet sheet;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
    private final List<Market> listNobis;
    private Double total;

    public String getFileName(List<Market> listNobis){
        String fileName="Shopping_Summary_from_"+listNobis.get(listNobis.size()-1).getDate().format(formatter)+"_To_"+listNobis.get(0).getDate().format(formatter)+".xlsx";
        return fileName;
    }

    public UserExcelExplorer(List<Market> listNobis) {
        this.listNobis = listNobis;
    }

    private CellStyle createStyle(short fontSize, boolean bold, Short bgColor, Short fontColor, HorizontalAlignment align) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(align);

        if (bgColor != null) {
            style.setFillForegroundColor(bgColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        Font font = workbook.createFont();
        font.setFontHeightInPoints(fontSize);
        font.setBold(bold);
        if (fontColor != null) font.setColor(fontColor);
        style.setFont(font);

        return style;
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);

        if (value instanceof Number) cell.setCellValue(((Number) value).doubleValue());
        else if (value instanceof LocalDate) cell.setCellValue(((LocalDate) value).format(formatter));
        else cell.setCellValue(value.toString());

        sheet.autoSizeColumn(col);
    }

    /** Export Excel as HttpServletResponse for download */
    public void export(HttpServletResponse response, Double total) throws IOException {
        this.total = total;
        sheet = workbook.createSheet(this.getFileName(listNobis) + LocalDate.now());
        createExcelContent();
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    /** Export Excel to byte[] for email attachment */
    public byte[] exportToByteArray(Double total) throws IOException {
        this.total = total;
        sheet = workbook.createSheet(this.getFileName(listNobis) + LocalDate.now());
        createExcelContent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private void createExcelContent() {

        DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy");

        // ===== Title Row =====
        Row headingRow = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        createCell(headingRow, 0,
                String.format("Shopping Summary - Items: %d, Total: %.2f BDT", listNobis.size(), total),
                createStyle((short)18, true,
                        IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex(),
                        null, HorizontalAlignment.CENTER));

        // ===== From-To Row =====
        Row paramRow = sheet.createRow(1);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
        createCell(paramRow, 0,
                String.format("From : %s, To : %s",
                        listNobis.get(listNobis.size()-1).getDate().format(headerFormatter),
                      listNobis.get(0).getDate().format(headerFormatter)),
                createStyle((short)12, true,
                        IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex(),
                        null, HorizontalAlignment.CENTER));

        // ===== Header Row =====
        Row headerRow = sheet.createRow(3);
        CellStyle headerStyle = createStyle((short)14, true,
                IndexedColors.DARK_BLUE.getIndex(),
                IndexedColors.WHITE.getIndex(),
                HorizontalAlignment.CENTER);

        String[] headers = {"SL", "Date", "Item Name", "Item Price"};
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }

        // ===== Data Styles =====
        CellStyle dataStyle = createStyle((short)12, false, null, null, HorizontalAlignment.CENTER);

        CellStyle priceStyle = createStyle((short)12, false, null, null, HorizontalAlignment.CENTER);
        priceStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        // ===== Data Rows =====
        int rowNum = 4;

        for (int i = 0; i < listNobis.size(); i++) {
            Market item = listNobis.get(i);
            Row row = sheet.createRow(rowNum++);

            // SL
            createCell(row, 0, i + 1, dataStyle);

            // Date → "3 Feb, 2026 , Sat"
            String dayShort = item.getDate()
                    .getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);

            String formattedDate = item.getDate().format(dateFormatter) + " , " + dayShort;
            createCell(row, 1, formattedDate, dataStyle);

            // Item Name
            createCell(row, 2, item.getItemName(), dataStyle);

            // Price
            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(item.getItemPrice());
            priceCell.setCellStyle(priceStyle);
        }

        // ===== Total Row =====
        if (!listNobis.isEmpty()) {
            Row totalRow = sheet.createRow(rowNum + 1);

            sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 2));

            CellStyle totalStyle = createStyle((short)13, true,
                    IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex(),
                    null, HorizontalAlignment.CENTER);

            createCell(totalRow, 0, "TOTAL", totalStyle);

            Cell totalPriceCell = totalRow.createCell(3);
            totalPriceCell.setCellValue(total);
            totalPriceCell.setCellStyle(priceStyle);
        }

        // ===== Auto size columns =====
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}