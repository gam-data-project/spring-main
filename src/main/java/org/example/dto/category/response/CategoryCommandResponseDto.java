package org.example.dto.category.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * 카테고리 CUD 공통 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryCommandResponseDto {
    /** 성공 여부 */
    private boolean success;

    /** 처리 메시지 */
    private String message;

    /** 대상 ID(생성/수정/삭제 대상) */
    private Long id;
}
