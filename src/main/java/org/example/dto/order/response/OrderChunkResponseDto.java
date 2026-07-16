package org.example.dto.order.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * 주문 chunk 저장 결과 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderChunkResponseDto {
    private boolean success;
    private String message;
    private String chunkId;
    private Integer chunkSeq;
    private Integer totalChunks;
    private Integer receivedOrderCount;
    private Integer savedSalesCount;
    private Integer savedDeliveryCount;
}
