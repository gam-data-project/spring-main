package org.example.dto.purchase.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** 매입 저장/수정 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PurchaseUpsertRequestDto {
    /** 대분류 */
    private String largeCategory;
    /** 중분류 */
    private String mediumCategory;
    /** 소분류 */
    private String smallCategory;
    /** 제품명(화면의 제품 select 값) */
    private String productName;
    /** 매입일 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate;
    /** 매입시간(선택) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime purchaseTime;
    /** 매입처 */
    private String supplierName;
    /** 단가 */
    private Integer unitCost;
    /** 수량 */
    private Integer quantity;
    /** 총금액 */
    private Integer totalCost;
}
