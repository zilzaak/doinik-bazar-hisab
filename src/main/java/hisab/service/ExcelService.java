package hisab.service;

import com.itextpdf.text.DocumentException;
import hisab.config.GoogleDriveBackupService;
import hisab.controller.UserPdfExporter;
import hisab.dto.MarketForm;
import hisab.entity.Market;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelService {
    @Autowired
    private GoogleDriveBackupService backupService;
    @Autowired
    private ItemCategoryService categoryService;
    private static final String EXCEL_FILE_PATH = "C:\\data/shopping_data.xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void writeToExcel(Market market) throws IOException {
        // If market has a non-null ID, try to find and update existing row
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
            String[] columns = {"id", "Product Name", "Price", "Date","Item Category"};
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

        if (market.getId() != null) {
            boolean updated = false;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Double xid = Double.parseDouble(row.getCell(0).toString());
                        if (xid.longValue() == market.getId()) {
                            setUpDatedData(row, market);
                            updated = true;
                            break;
                        }
                }
            }

            if (updated) {
                saveWorkbook(workbook, file);
                return;
            }
        }

        // If no ID or no existing row found, create new row
        Integer rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        Long idValue = (market.getId() != null && market.getId() != 0) ? market.getId() : rowNum.longValue();
        row.createCell(0).setCellValue(idValue);
        row.createCell(1).setCellValue(market.getItemName() != null ? market.getItemName() : "");
        row.createCell(2).setCellValue(market.getItemPrice() != null ? market.getItemPrice() : 0.0);
        if (market.getDate() != null) {
            row.createCell(3).setCellValue(market.getDate().format(DATE_FORMATTER));
        } else {
            row.createCell(3).setCellValue(LocalDate.now().format(DATE_FORMATTER));
        }
        row.createCell(4).setCellValue(market.getItemType() != null ? market.getItemType() : "--");

        System.out.println("Created new row with ID: " + idValue);

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
        saveWorkbook(workbook, file);
    }


    private void setUpDatedData(Row row, Market market) {
        // Update Product Name (column 1)
        Cell nameCell = row.getCell(1);
        if (nameCell == null) {
            nameCell = row.createCell(1);
        }
        nameCell.setCellValue(market.getItemName() != null ? market.getItemName() : "");

        // Update Price (column 2)
        Cell priceCell = row.getCell(2);
        if (priceCell == null) {
            priceCell = row.createCell(2);
        }
        priceCell.setCellValue(market.getItemPrice() != null ? market.getItemPrice() : 0.0);

        // Update Date (column 3)
        Cell dateCell = row.getCell(3);
        if (dateCell == null) {
            dateCell = row.createCell(3);
        }
        if (market.getDate() != null) {
            dateCell.setCellValue(market.getDate().format(DATE_FORMATTER));
        } else {
            dateCell.setCellValue(LocalDate.now().format(DATE_FORMATTER));
        }
    }

    private void saveWorkbook(Workbook workbook, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
        System.out.println("Data written to Excel file: " + EXCEL_FILE_PATH);
    }





    public List<Market> readExcelData(Long id , String name , LocalDate from , LocalDate to,String itemCat) throws IOException {
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
                   String xtype = row.getCell(4)!=null?row.getCell(4).toString():null;
                   Double xprice = Double.parseDouble(row.getCell(2).toString());
                   LocalDate xdate = LocalDate.parse(row.getCell(3).toString());

                   boolean idMatched=true;
                   boolean dateMatched=true;
                   boolean nameMatched=true;
                   boolean itemCatMatched=true;

                   Market m=new Market(xid,xname,xprice,xdate);
                   m.setItemType(xtype);
                   m.setDay(xdate.getDayOfWeek().name());
                   if(id!=null){
                       if(id.equals(xid)){
                           idMatched=true;
                       }else{
                           idMatched=false;
                       }
                   }

                   if(from!=null && to!=null){
                   if((xdate.equals(from) || xdate.isAfter(from)) && (xdate.equals(to) || xdate.isBefore(to))){
                          dateMatched=true;
                      }else{
                          dateMatched=false;
                      }
                   }
                   else if(from!=null && to==null){
                    if(xdate.equals(from) || xdate.isAfter(from)){
                        dateMatched=true;
                        }else{
                         dateMatched=false;
                           }
                     }

                 else if(to!=null && from==null){
                       if(xdate.equals(to) || xdate.isBefore(to)){
                            dateMatched=true;
                        }else{
                            dateMatched=false;
                            }
                                               }


            if(name!=null && !name.isBlank()){
               if(xname.toLowerCase().contains(name.toLowerCase())){
                   nameMatched=true;
               }else{
                   nameMatched=false;
               }
            }

            if(itemCat!=null && !itemCat.isBlank()){
                if(itemCat.equals(xtype)){
                    itemCatMatched=true;
                }else{
                    itemCatMatched=false;
                }
            }

            if(idMatched && dateMatched && nameMatched && itemCatMatched){
                list.add(m);
            }
        }

        workbook.close();
        fis.close();

        return list;
    }

    boolean existsByItemNameAndDateAndIdNotIn(String name , LocalDate date , Long id ) throws IOException {
        File file = new File(EXCEL_FILE_PATH);
        if (!file.exists()) {
            System.out.println("Excel file does not exist yet.");
            return false;
        }
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        boolean dataExist=false;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            Long dbId=((Double)(Double.parseDouble(row.getCell(0).toString()))).longValue();
            String dbName = row.getCell(1).toString();
            LocalDate dbDate = LocalDate.parse(row.getCell(3).toString());

            if(id==null){
                if(dbDate.equals(date) && dbName.equals(name)){
                    dataExist=true;
                    break;
                }
            }else{
                if(!id.equals(dbId) && dbDate.equals(date) && dbName.equals(name)){
                    dataExist=true;
                    break;
                }
            }

        }
        workbook.close();
        fis.close();
        return dataExist;
    }


    public List<Market> removeRowById(Long id) throws IOException {
        List<Market> latestList = new ArrayList<>();
        File file = new File(EXCEL_FILE_PATH);
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        int rowToRemove = -1;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header if needed
            Cell cellId = row.getCell(0);
            String name = row.getCell(1).toString();
            Double price = Double.parseDouble(row.getCell(2).toString());
            LocalDate date = LocalDate.parse(row.getCell(3).toString());

            if (cellId != null) {
                Double idcell = Double.parseDouble(cellId.toString());
                if (id.equals(idcell.longValue())) {
                    rowToRemove = row.getRowNum();
                    break;
                }else{
                    Market m = new Market(idcell.longValue(),name,price,date);
                    latestList.add(m);
                }
            }
        }

        if (rowToRemove != -1) {
            sheet.removeRow(sheet.getRow(rowToRemove));
            int lastRowNum = sheet.getLastRowNum();
            if (rowToRemove >= 0 && rowToRemove < lastRowNum) {
                sheet.shiftRows(rowToRemove + 1, lastRowNum, -1);
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        workbook.close();
        fis.close();
        fos.close();
        return latestList;
    }




    public Double totalPrice(List<Market> list){

        Double price=0.0;
        for(Market x : list ){
            price = price + Optional.ofNullable(x.getItemPrice()).orElse(0.0);
        }
        return price;

    }

    public ModelAndView addRowInForm(MarketForm form) {
        ModelAndView mv = new ModelAndView("index");
        String ind[] = form.getOperation().split("/");
        Market m2 = new Market(null,"",0.0,LocalDate.now());
        form.getMarkets().add(Integer.parseInt(ind[1]),m2);
        form.totalPrice=this.totalPrice(form.getMarkets());
        mv.addObject("marketForm",form);
        return mv;
    }

    public ModelAndView removeRowInForm(MarketForm form) throws IOException {
        ModelAndView mv = new ModelAndView("index");
        if(form.getMarkets().size()>1){
            String ind[] = form.getOperation().split("/");
            List<Market> list = new ArrayList<>();
            Integer i=0;
            Market obj = null;
            for(Market x :  form.getMarkets()){
                if(!i.equals(Integer.parseInt(ind[1]))){
                    list.add(x);
                }else{
                    obj=x;
                }
                i++;
            }

            if(obj!=null && obj.getId()!=null){
                List<Market> latestList = this.removeRowById(obj.getId());
                try{
                    this.keepFileBackupToDrive(latestList);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            form.setMarkets(list);
        }
        form.totalPrice=this.totalPrice(form.getMarkets());
        mv.addObject("marketForm",form);
        return mv;
    }

    public void keepFileBackupToDrive(List<Market> list) throws DocumentException, IOException {
        Double total = this.totalPrice(list);
        UserPdfExporter pdfExporter = new UserPdfExporter(list, total);
        byte[] pdfBytes=pdfExporter.exportToByteArray();
        try{
            this.backupService.syncPdfToDrive(1L,pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try{
            this.backupService.syncPdfToDrive(2L,pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public ModelAndView saveFormDataInExcell(MarketForm form) throws IOException {
        ModelAndView mv = new ModelAndView("index");
        StringBuilder errorMessage = new StringBuilder();
        boolean error=false;
        if(form.getDate()==null){
            errorMessage.append("Shopping date is missing ; ");
            error=true;
        }

        List<Market> validList = new ArrayList<>();
        for(Market m : form.getMarkets()){
            m.setDate(form.getDate());
            if(this.existsByItemNameAndDateAndIdNotIn(m.getItemName(),form.getDate(), m.getId())){
                    errorMessage.append(" ,"+errorMessage+m.getItemName()+" Is already entered in date "+form.getDate());
                   if(form.getTotalPrice()!=null) {
                       form.setTotalPrice(form.getTotalPrice() - m.getItemPrice());
                   }
                    continue;
                }

            if(m.getItemName().isBlank()){
                error=true;
                if(form.getTotalPrice()!=null){
                    form.setTotalPrice(form.getTotalPrice()-m.getItemPrice());
                }
                continue;
            }
            validList.add(m);
        }

/*        if(error){
            mv=new ModelAndView("error");
            mv.addObject("message",errorMessage.toString());
            return mv;
        }*/

        for(Market m : validList){
            this.writeToExcel(m);
        }
        form.totalPrice=this.totalPrice(validList);
        try{
          List<Market> list = this.readExcelData(null,null,null,null,null);
          this.keepFileBackupToDrive(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        form.setMarkets(validList);
        mv.addObject("marketForm",form);
        return mv;
    }

}