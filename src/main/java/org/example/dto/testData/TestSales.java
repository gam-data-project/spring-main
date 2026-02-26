package org.example.dto.testData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TestSales {
    private String productNameRaw;
    private Integer quantity;
    private Integer productTotal;
    private Integer unitPrice;
    private Boolean shippingIncluded;
}
