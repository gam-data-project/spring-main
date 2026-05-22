package org.example.dto.expense.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 엑셀 비용 조회 페이지 응답 DTO
 */
@Getter
@Builder
public class ExpenseExcelPageResponseDto {
    /** 현재 페이지(1-base) */
    private int page;

    /** 페이지 크기(고정 10 사용 권장) */
    private int size;

    /** 전체 건수 */
    private long totalCount;

    /** 전체 페이지 수 */
    private int totalPages;

    /** 현재 페이지 데이터 */
    private List<ExpenseExcelRowResponseDto> items;
}
