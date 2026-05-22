package org.example.dto.purchase.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 제품 매핑 조회(페이지) 요청 DTO
 * - 발생일 + 키워드(제품명 포함검색)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PurchaseProductMappingSearchPageRequestDto {private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 10;

    /** 발생일(매입날짜) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate occurredDate;

    /** 제품명 키워드(부분일치) */
    private String keyword;

    /** 페이지 번호(1-base) */
    private Integer page;

    /** 페이지 크기(기본 10, 최대 10) */
    private Integer size;

    /** 유효한 페이지 번호 반환 */
    public int getPageOrDefault() {
        return (page == null || page < 1) ? 1 : page;
    }

    /** 유효한 페이지 크기 반환 */
    public int getSizeOrDefault() {
        if (size == null || size < 1) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /** DB OFFSET 계산 */
    public int getOffset() {
        return (getPageOrDefault() - 1) * getSizeOrDefault();
    }

    /** DB LIMIT 계산 */
    public int getLimit() {
        return getSizeOrDefault();
    }
}
