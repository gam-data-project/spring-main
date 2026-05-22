package org.example.dto.expense.response;

import lombok.*;

/**
 * 비용 조회 셀렉트박스 공통 옵션 DTO.
 * value 하나만 내려서 option value/text에 그대로 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseOptionDto {
    /** 셀렉트박스 옵션 값 */
    private String value;
}
