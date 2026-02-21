package hisab.controller;

import hisab.entity.Market;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.*;


public class UserExcelExplorer {
    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFSheet sheet;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
    private final List<Market> listNobis;
    private Double total;

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

    public void export(HttpServletResponse response, Double total) throws IOException {
        this.total = total;
        sheet = workbook.createSheet("Shopping_" + LocalDate.now());

        // Heading
        Row headingRow = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        createCell(headingRow, 0,
                String.format("Shopping Summary - Items: %d, Total: BDT%.2f", listNobis.size(), total),
                createStyle((short)18, true, IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex(), null, HorizontalAlignment.CENTER));

        sheet.createRow(1); // Spacing

        // Header row - Navy blue background, white text
        Row headerRow = sheet.createRow(2);
        CellStyle headerStyle = createStyle((short)14, true, IndexedColors.DARK_BLUE.getIndex(),
                IndexedColors.WHITE.getIndex(), HorizontalAlignment.CENTER);
        String[] headers = {"SL", "Item Name", "Item Price (₹)", "Date"};
        for (int i = 0; i < headers.length; i++) createCell(headerRow, i, headers[i], headerStyle);

        // Data rows
        CellStyle dataStyle = createStyle((short)12, false, null, null, HorizontalAlignment.CENTER);
        CellStyle priceStyle = createStyle((short)12, false, null, null, HorizontalAlignment.CENTER);
        priceStyle.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));

        int rowNum = 3;
        for (int i = 0; i < listNobis.size(); i++) {
            Market item = listNobis.get(i);
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, i + 1, dataStyle);
            createCell(row, 1, item.getItemName(), dataStyle);

            Cell priceCell = row.createCell(2);
            priceCell.setCellValue(item.getItemPrice());
            priceCell.setCellStyle(priceStyle);

            createCell(row, 3, item.getDate(), dataStyle);
        }

        // Total row
        if (!listNobis.isEmpty()) {
            Row totalRow = sheet.createRow(rowNum + 1);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 1));
            CellStyle totalStyle = createStyle((short)13, true, IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex(),
                    null, HorizontalAlignment.CENTER);
            createCell(totalRow, 0, "TOTAL", totalStyle);

            Cell totalPriceCell = totalRow.createCell(2);
            totalPriceCell.setCellValue(total);
            totalPriceCell.setCellStyle(priceStyle);
            totalRow.createCell(3).setCellStyle(totalStyle);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}