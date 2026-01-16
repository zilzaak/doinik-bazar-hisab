package hisab.controller;


import hisab.dto.MarketForm;
import hisab.dto.SearchForm;
import hisab.entity.Market;
import hisab.repo.MarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Controller
public class HisabController {

    @Autowired
    MarketRepository marketRepository;

    @GetMapping("/")
    public ModelAndView home() {

        ModelAndView mv = new ModelAndView("index");
        MarketForm form = new MarketForm();
        List<Market> markets = marketRepository.findByDate(LocalDate.now());
        if(markets.size()>0){
            form.getMarkets().addAll(markets);
            form.setDate(markets.get(0).getDate());
        }else{
            Market m1 = new Market(null, LocalDate.now(), LocalTime.now(),"-",0.0,"-");
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
    public ModelAndView saveMarkets(@ModelAttribute MarketForm form) {
        form.setIndex(form.getMarkets().size()-1);
        ModelAndView mv = new ModelAndView("index");

        if(form.getOperation().contains("add")){
            String ind[] = form.getOperation().split("/");
            Market m2 = new Market(null, LocalDate.now(), LocalTime.now(),"",0.0,"");
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
                    marketRepository.delete(obj);
                }
                form.setMarkets(list);
            }
            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            return mv;
        }

        if(form.getOperation().equals("save")){
            for(Market m : form.getMarkets()){
                m.setDate(form.getDate());
             }
            marketRepository.saveAll(form.getMarkets());
            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            return mv;
        }

        if(form.getOperation().equals("datewiseShop")){
            List<Market> markets = marketRepository.findByDate(form.getDate());

            if(markets.size()<1){
                Market m1 = new Market(null, LocalDate.now(), LocalTime.now(),"-",0.0,"-");
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
        Integer pageNumber = params.containsKey("pageNumber")?Integer.parseInt(params.get("pageNumber")):1;
        Integer pageSize = params.containsKey("pageSize")?Integer.parseInt(params.get("pageSize")):200;

        LocalDate fromDate = params.containsKey("fromDate")?LocalDate.parse(params.get("fromDate")):null;
        LocalDate toDate = params.containsKey("toDate")?LocalDate.parse(params.get("toDate")):null;
        String itemName = params.containsKey("itemName")?params.get("itemName"):null;

        Pageable pageable = PageRequest.of(pageNumber-1,pageSize);
        Page<Market> page = marketRepository.allShoppingList(fromDate,toDate,itemName,pageable);

        Map<String,Object> response = new HashMap<>();
        response.put("markets",page.getContent());
        response.put("totalItems",page.getTotalElements());
        response.put("totalPages",page.getTotalPages());
        response.put("currentPage",pageNumber);
        response.put("pageSize",pageSize);
        mv.addObject("response",response);
        SearchForm sform=new SearchForm(fromDate,toDate,itemName);
        mv.addObject("sform",sform);
        return mv;
    }


}
