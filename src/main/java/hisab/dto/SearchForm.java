package hisab.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class SearchForm {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    private String itemName;

    public SearchForm(LocalDate fromDate, LocalDate toDate, String itemName) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.itemName = itemName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
