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
import java.util.ArrayList;
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

        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        } else {
            file.getParentFile().mkdirs();
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Shopping Data");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"id","Product Name","Price","Date"};

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


        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);

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





    public List<Market> readExcelData(Long id , String name , LocalDate from , LocalDate to) throws IOException {
        File file = new File(EXCEL_FILE_PATH);
        if (!file.exists()) {
            System.out.println("Excel file does not exist yet.");
            return null;
        }

        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        List<Market> list = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header
                   Double temp =  Double.parseDouble(row.getCell(0).toString());
                   Long xid=temp.longValue();
                   String xname = row.getCell(1).toString();
                   Double xprice = Double.parseDouble(row.getCell(2).toString());
                   LocalDate xdate = LocalDate.parse(row.getCell(3).toString());

                   boolean allFilterMacthed=false;
                   Market m=new Market(xid,xname,xprice,xdate);
                   if(id!=null){
                       if(id.equals(xid)){
                           allFilterMacthed=true;
                       }else{
                           allFilterMacthed=false;
                       }
                   }

                   if(from!=null){
                      if(xdate.equals(from) || xdate.isAfter(from)){
                          allFilterMacthed=true;
                      }else{
                          allFilterMacthed=false;
                      }
                   }

            if(to!=null){
                if(xdate.equals(to) || xdate.isBefore(to)){
                    allFilterMacthed=true;
                }else{
                    allFilterMacthed=false;
                }
            }

            if(name!=null && !name.isBlank()){
               if(xname.toLowerCase().contains(name.toLowerCase())){
                   allFilterMacthed=true;
               }else{
                   allFilterMacthed=false;
               }
            }

            if(name==null && id==null && from==null && to==null){
                allFilterMacthed=true;
            }

            if(allFilterMacthed){
                list.add(m);
            }

        }

        workbook.close();
        fis.close();

        return list;
    }

    public void saveAllInExcell(List<Market> markets) throws IOException {

        for(Market m : markets){
           this.writeToExcel(m);
        }
    }
}