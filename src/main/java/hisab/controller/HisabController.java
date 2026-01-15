package hisab.controller;

import hisab.dto.MarketForm;
import hisab.entity.Market;
import hisab.repo.MarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
            Market m2 = new Market(null, LocalDate.now(), LocalTime.now(),"-",0.0,"-");
            form.getMarkets().add(m1);
            form.getMarkets().add(m2);
            form.setDate(LocalDate.now());
        }
        form.totalPrice=this.totalPrice(form.getMarkets());
        mv.addObject("marketForm",form);
        mv.addObject("markets",form.getMarkets());
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
            mv.addObject("markets",form.getMarkets());
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
            mv.addObject("markets",form.getMarkets());
            return mv;
        }

        if(form.getOperation().equals("save")){
            for(Market m : form.getMarkets()){
                m.setDate(form.getDate());
             }
            marketRepository.saveAll(form.getMarkets());
            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            mv.addObject("markets",form.getMarkets());
            return mv;
        }

        if(form.getOperation().equals("datewiseShop")){
            List<Market> markets = marketRepository.findByDate(form.getDate());
            form.setMarkets(markets);
            form.totalPrice=this.totalPrice(form.getMarkets());
            mv.addObject("marketForm",form);
            mv.addObject("markets",form.getMarkets());
            return mv;
        }

        return mv;
    }


}
