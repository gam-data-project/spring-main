package org.example.dto.expense.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Excel 비용 조회 행 응답 DTO
 * - 업로드한 계좌 엑셀에서 추출/필터링한 결과 1건을 표현한다.
 */
@Getter
@Builder
public class NewExpenseExcelRowResponseDto {
    /** 비용 발생일 */
    private LocalDate expenseDate;

    /** 비용 발생시간 */
    private LocalTime expenseTime;

    /** 비용 발생처(설명/거래처) */
    private String description;

    /** 금액(총금액) */
    private Integer totalCost;
}
