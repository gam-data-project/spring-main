package org.example.dto.purchase.response;

import lombok.*;

/**
 * 매입 조회 셀렉트박스 공통 옵션 DTO.
 * value 하나만 내려서 option value/text로 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewPurchaseOptionDto {
    /** 셀렉트 옵션 값 */
    private String value;
}
