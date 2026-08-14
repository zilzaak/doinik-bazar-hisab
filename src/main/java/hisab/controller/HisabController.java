package hisab.controller;

import com.itextpdf.text.DocumentException;
import hisab.dto.MarketForm;
import hisab.dto.SearchForm;
import hisab.entity.Market;
import hisab.service.EmailService;
import hisab.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class HisabController {

    @Autowired
    ExcelService excelService;

    @Autowired
    EmailService emailService;

    @GetMapping("/")
    public ModelAndView home() throws IOException {
        ModelAndView mv = new ModelAndView("index");
        MarketForm form = new MarketForm();
        List<Market> markets = excelService.readExcelData(null,null,LocalDate.now(),LocalDate.now(),null);
        if(markets.size()>0){
            form.getMarkets().addAll(markets);
            form.setDate(markets.get(0).getDate());
        }else{
            Market m1 = new Market(null,"-",0.0,LocalDate.now());
            form.getMarkets().add(m1);
            form.setDate(LocalDate.now());
        }
        form.totalPrice=excelService.totalPrice(form.getMarkets());
        mv.addObject("marketForm",form);
        return mv;
    }



    @PostMapping("/saveMarkets")
    public ModelAndView saveMarkets(@ModelAttribute MarketForm form) throws IOException {
        form.setIndex(form.getMarkets().size()-1);
        ModelAndView mv = new ModelAndView("index");

        if(form.getOperation().contains("add")){
            return  excelService.addRowInForm(form);
        }

        if(form.getOperation().contains("delete")){
            return  excelService.removeRowInForm(form);
        }

        if(form.getOperation().equals("save")){
            return  excelService.saveFormDataInExcell(form);
        }

        if(form.getOperation().equals("datewiseShop")){
            List<Market> markets =  excelService.readExcelData(null,null,form.getDate(),form.getDate(),null);
            if(markets.size()<1){
                Market m1 = new Market(null,"-",0.0,LocalDate.now());
                markets.add(m1);
            }
            form.setMarkets(markets);
            form.totalPrice=excelService.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            return mv;
        }

        return mv;
    }




    @GetMapping("/download/pdf")
    public void exportToPdf(@RequestParam Map<String,String> params , HttpServletResponse response) throws IOException, DocumentException {
        ModelAndView mv = this.allShoppingList(params);
        Map<String, Object> modelResponse = (Map<String, Object>) mv.getModel().get("response");
        SearchForm sform = (SearchForm) mv.getModel().get("sform");
        List<Market> list = (List<Market>) modelResponse.get("markets");
        UserPdfExporter pdfExporter = new UserPdfExporter(list, sform.getTotalPrice());
        String fileName=pdfExporter.getFileName(list) ;
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+fileName;
        response.setHeader(headerKey, headerValue);
        pdfExporter.export(response);
        try{
            byte[] pdfBytes = pdfExporter.exportToByteArray();
            List<String> recipients = Arrays.asList("jebafariha102@gmail.com","jebafariha705@gmail.com");
            emailService.sendPdf(pdfBytes,recipients);
        } catch (Exception e) {
        }

    }


    @GetMapping("/download/excel")
    public void exportToExcel(HttpServletResponse response, @RequestParam Map<String,String> params) throws IOException {
        ModelAndView mv = this.allShoppingList(params);
        Map<String, Object> modelResponse = (Map<String, Object>) mv.getModel().get("response");
        SearchForm sform = (SearchForm) mv.getModel().get("sform");
        List<Market> list = (List<Market>) modelResponse.get("markets");

        UserExcelExplorer excelExporter = new UserExcelExplorer(list);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+excelExporter.getFileName(list);
        response.setHeader(headerKey, headerValue);
        excelExporter.export(response,sform.getTotalPrice());
        try {
            List<String> recipients = List.of("jebafariha102@gmail.com", "jebafariha705@gmail.com"); // Add your recipients
            emailService.sendExcelReportEmail(recipients,"Shopping Summary Report", "Shopping summary report in excel format ", list, sform.getTotalPrice());
            System.out.println("Excel report emailed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send Excel report via email: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ModelAndView allShoppingList(@RequestParam Map<String,String> params) {
        ModelAndView mv = new ModelAndView("shoppings");
        Integer totalItems=null;
        Integer totalPages=null;
        Integer pageNumber = 1;
        Integer pageSize = 100;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        String itemName = null;
        String itemCategory = null; //utility bill ,

        if(params.containsKey("pageNumber") && !params.get("pageNumber").isBlank()){
            pageNumber=Integer.parseInt(params.get("pageNumber"));
        }

        if(params.containsKey("pageSize") && !params.get("pageSize").isBlank()){
            pageSize=Integer.parseInt(params.get("pageSize"));
        }

        if(params.containsKey("fromDate") && params.get("fromDate").length() > 9 ){
            fromDate=LocalDate.parse(params.get("fromDate"));
        }
        if(params.containsKey("toDate") && params.get("toDate").length() > 9 ){
            toDate = LocalDate.parse(params.get("toDate"));
        }
        if(params.containsKey("itemName") && !params.get("itemName").isBlank()){
            itemName = params.get("itemName");
        }

        if(params.containsKey("itemCat") && !params.get("itemCat").isBlank()){
            itemCategory=params.get("itemCat");
        }

           List<Market>  list = new ArrayList<>();
           List<Market>  filteredList = new ArrayList<>();
            try{
                filteredList = excelService.readExcelData(null,itemName,fromDate,toDate,itemCategory);
                totalItems=filteredList.size();
                totalPages=(int) Math.ceil((double) totalItems / pageSize);
                int startIndex = (pageNumber - 1) * pageSize;
                int endIndex = Math.min(
                        startIndex + pageSize,
                        totalItems
                );
                list = filteredList.subList(startIndex,endIndex);
                list = list.stream().sorted(Comparator.comparing(Market::getDate).reversed()).collect(Collectors.toList());
            }catch (Exception e){
            }

        Map<String,Object> response = new HashMap<>();
        response.put("markets",list);
        mv.addObject("response",response);
        SearchForm sform=new SearchForm(fromDate,toDate,itemName);
        sform.setTotalPages(totalPages);
        sform.setTotalItems(totalItems);
        sform.setPageNumber(pageNumber);
        sform.setPageSize(pageSize);
        sform.setTotalPrice(excelService.totalPrice(list));
        mv.addObject("sform",sform);
        return mv;
    }
}
