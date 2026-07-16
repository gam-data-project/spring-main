package org.example.dto.order.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * 주문 1건 내부의 배송비 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderDeliveryDto {
    private Boolean shippingIncluded;
    private Integer totalDeliveryFee;
    private Integer shippingCount;
    private Integer unitPrice;
}
