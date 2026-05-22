package org.example.dto.category.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * 카테고리 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewCategoryUpsertRequestDto {
    /** 대분류 */
    private String largeCategory;

    /** 중분류 */
    private String mediumCategory;

    /** 소분류 */
    private String smallCategory;
}
