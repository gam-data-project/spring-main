package org.example.dto.settlementReport.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SettlementReportRowDto {
    private String ym;
    private Long salesAmount;
    private Long purchaseAmount;
    private Long expenseAmount;
    private Long profit;
}
