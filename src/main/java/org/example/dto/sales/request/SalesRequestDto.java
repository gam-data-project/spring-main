package org.example.dto.sales.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SalesRequestDto {
    //pk useGeneratedKeys로 채워짐
    private Long id;
    //주문번호
    private String orderNumber;
    //플랫폼이름
    private String platform;
    //제품명
    private String productNameRaw;
    // 제품 id (null 가능)
    private Long productId;
    //제품 수량
    private int quantity;
    //상품 금액 총합
    private int productTotal;
    //제품 단가
    private int unitPrice;
    //배송비 포함여부
    private boolean shippingIncluded;   // JSON is_shipping_included 자동 매핑
    //주문 날짜
    private String orderDate; // "yyyy-MM-dd"
}

