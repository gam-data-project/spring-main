package org.example.dto.renewal;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewCategoryListItemDto {
    /** PK */
    private Long id;

    /** 대분류 */
    private String largeCategory;

    /** 중분류 */
    private String mediumCategory;

    /** 소분류 */
    private String smallCategory;

    /** 생성일시 */
    private LocalDateTime createdAt;

    /** 수정일시 */
    private LocalDateTime updatedAt;
}
