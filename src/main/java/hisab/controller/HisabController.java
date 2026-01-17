package hisab.controller;

import hisab.dto.MarketForm;
import hisab.dto.SearchForm;
import hisab.entity.Market;
import hisab.repo.MarketRepository;
import hisab.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Controller
public class HisabController {

    @Autowired
    MarketRepository marketRepository;

    @Autowired
    ExcelService excelService;

    @GetMapping("/")
    public ModelAndView home() {

        ModelAndView mv = new ModelAndView("index");
        MarketForm form = new MarketForm();
        List<Market> markets = marketRepository.findByDate(LocalDate.now());
        if(markets.size()>0){
            form.getMarkets().addAll(markets);
            form.setDate(markets.get(0).getDate());
        }else{
            Market m1 = new Market(null,"-",0.0,LocalDate.now());
            form.getMarkets().add(m1);
            form.setDate(LocalDate.now());
        }
        form.totalPrice=this.totalPrice(form.getMarkets());
        mv.addObject("marketForm",form);
        return mv;
    }

      private Double totalPrice(List<Market> list){

        Double price=0.0;
        for(Market x : list ){
            price = price + Optional.ofNullable(x.getItemPrice()).orElse(0.0);
        }
        return price;

      }

    @PostMapping("/saveMarkets")
    public ModelAndView saveMarkets(@ModelAttribute MarketForm form) throws IOException {
        form.setIndex(form.getMarkets().size()-1);
        ModelAndView mv = new ModelAndView("index");

        if(form.getOperation().contains("add")){
            String ind[] = form.getOperation().split("/");
            Market m2 = new Market(null,"",0.0,LocalDate.now());
            form.getMarkets().add(Integer.parseInt(ind[1]),m2);
            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            return mv;
        }

        if(form.getOperation().contains("delete")){
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
                    //marketRepository.delete(obj);


                }
                form.setMarkets(list);
            }
            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            return mv;
        }

        if(form.getOperation().equals("save")){

            StringBuilder errorMessage = new StringBuilder();

            boolean error=false;
            if(form.getDate()==null){
                errorMessage.append("Shopping d0ate is missing ; ");
                error=true;
            }
            for(Market m : form.getMarkets()){
                m.setDate(form.getDate());
                if(m.getId()!=null){
                    if(marketRepository.existsByItemNameAndDateAndIdNotIn(m.getItemName(),form.getDate(),Arrays.asList(m.getId()))){
                        errorMessage.append(" ,"+errorMessage+m.getItemName()+" Is already entered in date "+form.getDate());
                        error=true;
                        break;
                    }

                }else{
                    if(marketRepository.existsByItemNameAndDate(m.getItemName(),form.getDate())){
                        errorMessage.append(" ,"+errorMessage+m.getItemName()+" Is already entered in date "+form.getDate());
                        error=true;
                        break;
                    }
                }
                if(m.getItemName().isBlank()){
                    error=true;
                }
             }

            if(error){
                mv=new ModelAndView("error");
                mv.addObject("message",errorMessage.toString());
                return mv;
            }

           // marketRepository.saveAll(form.getMarkets());\
            excelService.saveAllInExcell(form.getMarkets());

            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            return mv;
        }

        if(form.getOperation().equals("datewiseShop")){
            List<Market> markets = marketRepository.findByDate(form.getDate());

            if(markets.size()<1){
                Market m1 = new Market(null,"-",0.0,LocalDate.now());
                markets.add(m1);
            }
            form.setMarkets(markets);
            form.totalPrice=this.totalPrice(form.getMarkets());
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
        sform.setTotalPrice(this.totalPrice(list));
        mv.addObject("sform",sform);
        return mv;
    }


}
