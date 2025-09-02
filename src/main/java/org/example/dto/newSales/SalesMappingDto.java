package org.example.dto.newSales;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.domain.Platform;

import java.time.LocalDate;

/**
 * @author ARa
 *
 * sales 테이블과 product 테이블을 매핑 하기 위해
 * sales 테이블에서 조회한 data
 */
@Getter
@Setter
@Builder
public class SalesMappingDto {
    //salse 테이블에 product_id 저장할 때 사용
    private Long id;
    //UK
    private String productNameRaw;
    //매핑 할 값
    private Long productId;
    //UK
    private Integer unitPrice;
    //UK
    private Platform platform;
    //배송비가 포함된 제품인가
    private Boolean shippingIncluded;
    //첫 판매일 저장
    private LocalDate orderDate;   // 필요하다면 LocalDate 권장
}
