package org.example.dto.expense.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 비용 목록 조회(페이징) 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseSearchRequestDto {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 10;

    /** 대분류(요구사항상 고정: 비용) */
    private String largeCategory;

    /** 중분류(ALL/전체면 조건 제외) */
    private String mediumCategory;

    /** 소분류(ALL/전체면 조건 제외) */
    private String smallCategory;

    /** 유형(ALL/전체면 조건 제외) */
    private String expenseType;

    /** 시작일(없으면 날짜 하한 조건 제외) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 끝일(없으면 날짜 상한 조건 제외) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 페이지 번호(1-base) */
    private Integer page;

    /** 페이지 크기(기본 10, 최대 10) */
    private Integer size;

    /**
     * 유효한 페이지 번호를 반환한다.
     *
     * @return 최소 1 보장 페이지 번호
     */
    public int getPageOrDefault() {
        return (page == null || page < 1) ? 1 : page;
    }

    /**
     * 유효한 페이지 크기를 반환한다.
     *
     * @return 기본 10, 최대 10으로 고정된 페이지 크기
     */
    public int getSizeOrDefault() {
        if (size == null || size < 1) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /**
     * DB OFFSET 값을 계산한다.
     *
     * @return offset
     */
    public int getOffset() {
        return (getPageOrDefault() - 1) * getSizeOrDefault();
    }

    /**
     * DB LIMIT 값을 반환한다.
     *
     * @return limit
     */
    public int getLimit() {
        return getSizeOrDefault();
    }
}
