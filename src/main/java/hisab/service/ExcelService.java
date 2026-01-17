package hisab.service;

import hisab.entity.Market;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelService {

    private static final String EXCEL_FILE_PATH = "data/shopping_data.xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void writeToExcel(Market market) throws IOException {
        File file = new File(EXCEL_FILE_PATH);
        Workbook workbook;
        Sheet sheet;

        // Check if file exists
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        } else {
            // Create directory if it doesn't exist
            file.getParentFile().mkdirs();
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Shopping Data");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"SL No","Product Name","Price","Date"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
        }

        // Get the next row number (last row + 1)
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);

        // SL No (Row number starting from 1)
        row.createCell(0).setCellValue(rowNum);

        // Product Name
        row.createCell(1).setCellValue(market.getItemName() != null ? market.getItemName() : "");
        row.createCell(2).setCellValue(market.getItemPrice() != null ? market.getItemPrice() : 0.0);
        if (market.getDate() != null) {
            row.createCell(3).setCellValue(market.getDate().format(DATE_FORMATTER));
        } else {
            row.createCell(3).setCellValue(LocalDate.now().format(DATE_FORMATTER));
        }

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();

        System.out.println("Data written to Excel file: " + EXCEL_FILE_PATH);
    }

    // Optional: Method to read existing data from Excel
    public void readExcelData() throws IOException {
        File file = new File(EXCEL_FILE_PATH);
        if (!file.exists()) {
            System.out.println("Excel file does not exist yet.");
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        System.out.println("\n=== Existing Shopping Data ===");
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            System.out.printf("SL No: %d, Date: %s, Time: %s, Product: %s, Price: %.2f, Desc: %s%n",
                    (int) row.getCell(0).getNumericCellValue(),
                    row.getCell(1).getStringCellValue(),
                    row.getCell(2).getStringCellValue(),
                    row.getCell(3).getStringCellValue(),
                    row.getCell(4).getNumericCellValue(),
                    row.getCell(5).getStringCellValue()
            );
        }

        workbook.close();
        fis.close();
    }

    public void saveAllInExcell(List<Market> markets) throws IOException {

        for(Market m : markets){
           this.writeToExcel(m);
        }
    }
}