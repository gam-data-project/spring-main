package org.example.dto.renewal;


import lombok.*;

/**
 * 카테고리 셀렉트 박스 옵션 DTO.
 * value 하나만 내려서 프론트 select option에 바인딩할 때 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryOptionDto {

    /** 셀렉트 박스 옵션 값(대/중/소 분류 문자열) */
    private String value;
}
