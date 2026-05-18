package org.example.dto.renewal;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * 카테고리 목록 페이징 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewCategoryPageResponseDto {
    /** 현재 페이지(1-base) */
    private Integer page;

    /** 페이지 크기 */
    private Integer size;

    /** 전체 건수 */
    private Long totalCount;

    /** 전체 페이지 수 */
    private Long totalPages;

    /** 현재 페이지 데이터 */
    private List<NewCategoryListItemDto> items;
}
