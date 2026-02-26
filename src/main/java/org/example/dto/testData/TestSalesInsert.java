package org.example.dto.testData;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TestSalesInsert {
    private String orderNumber;
    private String platform;
    private String productNameRaw;
    private Integer quantity;
    private Integer productTotal;
    private Integer unitPrice;
    private Boolean shippingIncluded;
    private LocalDate orderDate;   // DATE 타입
}
