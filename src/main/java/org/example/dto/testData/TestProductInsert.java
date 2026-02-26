package org.example.dto.testData;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TestProductInsert {
    private String productName;
    private Integer unitPrice;
    private Integer unitCnt;
    private String platform;
    private Boolean shippingIncluded;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;

}
