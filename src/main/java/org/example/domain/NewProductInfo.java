package org.example.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewProductInfo {
    //pk useGeneratedKeys로 채워짐
    private Long id;
    //제품명
    private String productName;
    //제품 단가
    private Integer unitPrice;
    //제품 수량(매입시 비교)
    private Integer unitCnt;
    //플랫폼이름
    private Platform platform;
    //카테고리와 매핑하는 속성
    private Long categoryId;
    //배송비 포함 여부
    private Boolean shippingIncluded;
    //판매 시작 날짜
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    //판매 완료 날짜
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    //판매 중인지 판별
    private Boolean active;

}




