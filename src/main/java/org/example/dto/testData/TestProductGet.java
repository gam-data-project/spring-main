package org.example.dto.testData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestProductGet {
    private String productName;
    private Integer unitPrice;
    private Integer unitCnt;
    private Boolean shippingIncluded;
}
