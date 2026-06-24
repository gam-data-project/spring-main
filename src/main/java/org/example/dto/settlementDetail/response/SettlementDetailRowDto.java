package org.example.dto.settlementDetail.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SettlementDetailRowDto {
    private LocalDate dt;
    private String gb;
    private String largeCategory;
    private String mediumCategory;
    private String smallCategory;
    private Long amount;
}
