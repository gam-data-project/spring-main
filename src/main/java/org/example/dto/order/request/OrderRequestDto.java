package org.example.dto.order.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * chunk 내부의 주문 1건을 표현하는 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderRequestDto {
    /**
     * 향후 멱등키로 활용할 수 있는 메시지 ID.
     * 현재 1차 구현에서는 DB 컬럼에 저장하지 않고 로그/확장 포인트로만 둔다.
     */
    private String orderMessageId;

    private String orderNumber;
    private String platform;
    private String orderDate;
    private List<OrderSalesItemDto> salesItems;
    private OrderDeliveryDto delivery;
}
