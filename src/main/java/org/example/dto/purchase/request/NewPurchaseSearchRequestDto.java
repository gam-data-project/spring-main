package org.example.dto.purchase.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 매입 목록 조회(페이징) 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewPurchaseSearchRequestDto {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 10;

    /** 대분류 (예: 매입, 전체) */
    private String largeCategory;

    /** 중분류 (예: 전체) */
    private String mediumCategory;

    /** 소분류 (예: 전체) */
    private String smallCategory;

    /** 매입처 (예: 전체) */
    private String supplierName;

    /** 시작일 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 종료일 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

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
