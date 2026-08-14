package hisab.dto;

import hisab.entity.Market;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ApiDTO {
    private Integer totalItems;
    private Integer totalPages;
    private Integer pageNumber;
    private Integer pageSize;
    private List<Market> list=new ArrayList<>();

}
