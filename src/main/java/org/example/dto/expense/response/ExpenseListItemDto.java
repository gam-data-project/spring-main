package org.example.dto.expense.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 비용 목록 행 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseListItemDto {
    /** PK */
    private Long id;

    /** 대분류 */
    private String largeCategory;

    /** 중분류 */
    private String mediumCategory;

    /** 소분류 */
    private String smallCategory;

    /** 유형 */
    private String expenseType;

    /** 비용 발생처 */
    private String description;

    /** 단가 */
    private Integer unitCost;

    /** 수량 */
    private Integer quantity;

    /** 총금액 */
    private Integer totalCost;

    /** 발생일 */
    private LocalDate expenseDate;

    /** 발생시간 */
    private LocalTime expenseTime;

    /** 생성일시 */
    private LocalDateTime createdAt;

    /** 수정일시 */
    private LocalDateTime updatedAt;
}
