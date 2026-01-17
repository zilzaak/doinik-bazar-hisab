package hisab.controller;

import hisab.dto.MarketForm;
import hisab.dto.SearchForm;
import hisab.entity.Market;
import hisab.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;


@Controller
public class HisabController {

    @Autowired
    ExcelService excelService;

    @GetMapping("/")
    public ModelAndView home() throws IOException {
        ModelAndView mv = new ModelAndView("index");
        MarketForm form = new MarketForm();
        List<Market> markets = excelService.readExcelData(null,null,LocalDate.now(),LocalDate.now());
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
            List<Market> markets =  excelService.readExcelData(null,null,form.getDate(),form.getDate());
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


    @GetMapping("/list")
    public ModelAndView allShoppingList(@RequestParam Map<String,String> params) {
        ModelAndView mv = new ModelAndView("shoppings");
        Integer pageNumber = 1;
        Integer pageSize = 200;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        String itemName = null;

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

           List<Market>  list = new ArrayList<>();
            try{
                list = excelService.readExcelData(null,itemName,fromDate,toDate);
            }catch (Exception e){

            }

        Map<String,Object> response = new HashMap<>();
        response.put("markets",list);
        response.put("totalItems",list.size());
        response.put("totalPages",list.size()/pageSize);
        response.put("currentPage",pageNumber);
        response.put("pageSize",pageSize);
        mv.addObject("response",response);
        SearchForm sform=new SearchForm(fromDate,toDate,itemName);
        sform.setTotalPrice(excelService.totalPrice(list));
        mv.addObject("sform",sform);
        return mv;
    }


}
