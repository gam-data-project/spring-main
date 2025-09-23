package org.example.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewPurchaseInfo {
    private long id;
    //제품 id
    private long productId;
    // 매입 날짜
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    // 매입 수량
    private Integer quantity;
    // 매입 단가
    private Integer unitCost;
    // 총 매입 금액
    private Integer totalCost;
    // 거래처명
    private String supplierName;

}
