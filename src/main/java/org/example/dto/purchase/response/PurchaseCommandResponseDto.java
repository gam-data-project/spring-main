package org.example.dto.purchase.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/** 매입 CUD 공통 응답 DTO */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PurchaseCommandResponseDto {
    /** 성공 여부 */
    private boolean success;
    /** 처리 메시지 */
    private String message;
    /** 대상 ID */
    private Long id;
}
