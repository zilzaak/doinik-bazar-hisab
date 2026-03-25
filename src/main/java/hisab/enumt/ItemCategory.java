package hisab.enumt;

public enum ItemCategory {
     Food("Food"),
     Fatiha("Fatiha"),
     Transport("Transport"),
     Utility("Utility"),
     Outfit("Outfit"),
     Others("Others");

     private  String name;
     ItemCategory(String value){
         this.name=value;
    }
    public String getKey(){
       return this.getKey();
    }

    public String getValue(){
        return this.name();
    }
}
