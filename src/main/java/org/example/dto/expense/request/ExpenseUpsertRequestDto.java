package org.example.dto.expense.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 비용 저장/수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseUpsertRequestDto {
    /** 대분류(고정: 비용) */
    private String largeCategory;

    /** 중분류 */
    private String mediumCategory;

    /** 소분류 */
    private String smallCategory;

    /** 비용 유형(enum 문자열: SHIPPING, PACKAGING ...) */
    private String expenseType;

    /** 발생일 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate;

    /** 발생시간(선택) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime expenseTime;

    /** 비용 발생처 */
    private String description;

    /** 단가(선택) */
    private Integer unitCost;

    /** 수량(선택) */
    private Integer quantity;

    /** 총금액(필수) */
    private Integer totalCost;
}
