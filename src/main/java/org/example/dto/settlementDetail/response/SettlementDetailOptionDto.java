package org.example.dto.settlementDetail.response;


import lombok.*;

/**
 * 정산 상세조회 셀렉트박스 공통 옵션 DTO.
 * value 하나만 내려서 option value/text로 그대로 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementDetailOptionDto {
    /** 셀렉트박스 옵션 값 */
    private String value;
}
