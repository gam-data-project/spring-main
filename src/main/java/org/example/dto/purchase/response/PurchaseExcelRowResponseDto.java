package org.example.dto.purchase.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 엑셀 매입 조회 행 DTO
 */
@Getter
@Builder
public class PurchaseExcelRowResponseDto {
    /** 날짜 */
    private LocalDate purchaseDate;
    /** 시간 */
    private LocalTime purchaseTime;
    /** 매입처 */
    private String supplierName;
    /** 금액 */
    private Integer totalCost;
}
