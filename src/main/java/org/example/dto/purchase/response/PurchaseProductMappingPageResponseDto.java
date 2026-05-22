package org.example.dto.purchase.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 제품 매핑 조회 페이지 응답 DTO
 */
@Getter
@Builder
public class PurchaseProductMappingPageResponseDto {
    /** 현재 페이지(1-base) */
    private int page;
    /** 페이지 크기(고정 10) */
    private int size;
    /** 전체 건수 */
    private long totalCount;
    /** 전체 페이지 수 */
    private long totalPages;
    /** 현재 페이지 데이터 */
    private List<PurchaseProductMappingListItemDto> items;
}
