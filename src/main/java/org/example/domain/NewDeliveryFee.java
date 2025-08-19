package org.example.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewDeliveryFee {
    //pk useGeneratedKeys로 채워짐
    private Long id;
    //주문번호
    private String orderNumber;
    //플랫폼이름
    private String platform;
    //배송비 포함여부
    private boolean shippingIncluded;   // JSON is_shipping_included 자동 매핑
    //총 배송금액
    private int totalDeliveryFee;
    //배송비 개수
    private int shippingCount;
    //주문 날짜
    private String orderDate; // "yyyy-MM-dd"

}
