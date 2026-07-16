package org.example.dto.purchase.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * 매입 목록 페이징 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PurchasePageResponseDto {
    private Integer page;
    private Integer size;
    private Long totalCount;
    private Long totalPages;
    private List<PurchaseListItemDto> items;
}
