package org.example.dto.purchase.response;

import lombok.*;

import java.time.LocalDate;

/**
 * 제품 매핑 조회 목록 행 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewPurchaseProductMappingListItemDto {
    /** 대분류 */
    private String largeCategory;
    /** 중분류 */
    private String mediumCategory;
    /** 소분류 */
    private String smallCategory;
    /** 제품명 */
    private String productName;
    /** 시작일 */
    private LocalDate startDate;
    /** 종료일 */
    private LocalDate endDate;
    /** 활성여부 라벨(활성/비활성) */
    private String activeLabel;
}
