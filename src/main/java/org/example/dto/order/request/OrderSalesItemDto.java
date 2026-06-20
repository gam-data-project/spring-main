package org.example.dto.order.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * 주문 1건 내부의 판매 상품 1건 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderSalesItemDto {
    /**
     * 주문 내 상품 순번.
     * 현재 sales 테이블에는 별도 저장하지 않는다.
     */
    private Integer itemSeq;

    private String productNameRaw;
    private Integer quantity;
    private Integer productTotal;
    private Integer unitPrice;
    private Boolean shippingIncluded;
}
