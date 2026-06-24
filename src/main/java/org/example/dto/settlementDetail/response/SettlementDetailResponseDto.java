package org.example.dto.settlementDetail.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SettlementDetailResponseDto {
    private List<SettlementDetailRowDto> items;
    private Long deductedDeliveryFee;
    private Long salesTotal;
    private Long purchaseTotal;
    private Long profit;
}
