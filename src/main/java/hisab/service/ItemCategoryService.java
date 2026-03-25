package hisab.service;

import hisab.enumt.ItemCategory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
@Service
public class ItemCategoryService {

    public List<String> foodList = Arrays.asList(
            "vegetable","medecine","osud","tablet","capsule","file","syrup",
         "shaak","shak","chira","kola","banana","ginger","ada","deros","dheros","dherus","biriyani","birani","burhani",
         "kolmi","palong","holud","garlic","rosun","morich","moreech","bred","biscuit","biscit","butter","ponir",
         "lebo","lebu","lemon","pata","long","zira","mosola","masala","halim","semai","lichi","lichu","chocolate",
         "corn","cornflower","cornflour","kauli flower","kopi","kobi","capsicum","boroi","anaros",
         "tomato","tamato","seem","soi","choi","sheem","sim","aloo","alu","alo","potato","kissmiss","peanut","nut","date",
         "borboti","choi","soi","lossoi","boroboti","korola","korla","kakrol","karkol","badam","katbadam","almond",
          "papa","pepe","papaya","begun","baigon","baygon", "lau","lao","laao","kochu","loti","bendi","bandi","vendi",
          "gajor","carrot","carot","rice","chaol","chaal","chal","daal","dal","daol","soya","khira","sosa","shosha",
          "shoris","soris","mastered","tel","oil", "tel","ata","moida","flour","gom","suji","beson","piyara","guava",
          "green tea","tea","coffee","cha pata","cha","condensed","water","pani","melon","watermelon","cucumber",
          "milk","dud","chini","sugar","motor suti","doi","egg","dim","apple","fruit","dalim","komola","orange",
          "koida","jhinga","chichinga","fish","maach","murgi","goru","cow","hen","mangso","meat",
           "strawberry","berry","jam","mango","aam","katal","jackfruit","dragon","peyaz","piaj","peyaj","piaj","piaz",
            "muri","hash","nodles","noodles","tetul","singara","chop","puri",
            "beguni","amloki","peyara","piara","achar","hojmi","hozmi","ice",
            "icecream","seven up","mojo","sprite","pan","jorda","laddo","tormuj",
            "tormuz","turmaric","curcuma","kurcuma","kur","khejur","gur","jilapi","gilapi","vinegar","angur","pudina",
            "pesta","badam","kismis","methi","seed","chia","honey","modhu");


    public List<String> fatihaGoods = Arrays.asList(
         "pampas","medecine","fatiha","toy","khelna"
    );

    public List<String> outFitGoods = Arrays.asList(
     "shirt","pant","juta","muja","shampoo","saban","soap","gel","facewash","face","mask","juta","shoe","sandel","lux","fair & lovely","clip","rover","bag",
      "jeans","pant","shirt","three peice","dress","kapor","phone","set","snow"
    );

    public List<String> transport = Arrays.asList(
           "train","bus","journey","ticket","transport"
    );

    public List<String> utilityBill = Arrays.asList("bill","vara","wifi bill","basa bill","current","basa");


    public  String  getCategoryFromName(String itemName){
            if(this.isFoodCat(itemName)){
               return ItemCategory.Food.getValue();
            }
            if(this.isFatihaCat(itemName)){
                return ItemCategory.Fatiha.getValue();
            }

            if(this.isOutfitCat(itemName)){
                return ItemCategory.Outfit.getValue();
            }

            if(this.isTransportCat(itemName)){
                return  ItemCategory.Transport.getValue();
            }
            if(this.isUtilityCat(itemName)){
                return ItemCategory.Utility.getValue();
            }

           return ItemCategory.Others.getValue();
    }


    public boolean isFoodCat(String itemName){
        for(String str : this.foodList){
            if(itemName.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }

    public boolean isFatihaCat(String itemName){
        for(String str : this.fatihaGoods){
            if(itemName.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }


    public boolean isTransportCat(String itemName){
        for(String str : this.transport){
            if(itemName.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }


    public boolean isOutfitCat(String itemName){
        for(String str : this.outFitGoods){
            if(itemName.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }

    public boolean isUtilityCat(String itemName){
        for(String str : this.utilityBill){
            if(itemName.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }

    public boolean isOthersCat(String itemName){
        if(!this.isFoodCat(itemName)&&
        !this.isFatihaCat(itemName) &&
        !this.isOutfitCat(itemName) &&
        !this.isTransportCat(itemName)&&
        !this.isUtilityCat(itemName)){
            return true;
        }
        return false;
    }


    public boolean isItemUnderSearchedCategory(String itemName,String searchedCategory){
        if(searchedCategory.equals(ItemCategory.Food.getValue())){
            return this.isFoodCat(itemName);
        }
        if(searchedCategory.equals(ItemCategory.Fatiha.getValue())){
            return this.isFatihaCat(itemName);
        }

        if(searchedCategory.equals(ItemCategory.Outfit.getValue())){
            return this.isOutfitCat(itemName);
        }

        if(searchedCategory.equals(ItemCategory.Transport.getValue())){
            return this.isTransportCat(itemName);
        }

        if(searchedCategory.equals(ItemCategory.Utility.getValue())){
            return this.isUtilityCat(itemName);
        }

        if(searchedCategory.equals(ItemCategory.Others.getValue())){
            return this.isOthersCat(itemName);
        }
        return false;
    }

}
