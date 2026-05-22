package org.example.dto.purchase.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 매입 목록 행 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewPurchaseListItemDto {
    private Long id;
    private String largeCategory;
    private String mediumCategory;
    private String smallCategory;
    private String supplierName;
    private String productName;
    private Integer unitCost;
    private Integer quantity;
    private Integer totalCost;
    private LocalDate purchaseDate;
    private LocalTime purchaseTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
