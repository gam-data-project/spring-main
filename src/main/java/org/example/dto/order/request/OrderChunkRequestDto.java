package org.example.dto.order.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;


/**
 * Python crawler가 전송한 주문 chunk 요청 DTO.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderChunkRequestDto {
    private String chunkId;
    private Integer chunkSeq;
    private Integer totalChunks;
    private List<OrderRequestDto> orders;
}
